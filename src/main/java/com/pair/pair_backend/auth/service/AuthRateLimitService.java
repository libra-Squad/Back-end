package com.pair.pair_backend.auth.service;

import com.pair.pair_backend.global.exception.BusinessException;
import com.pair.pair_backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthRateLimitService {
    private static final Duration IP_WINDOW = Duration.ofMinutes(1);
    private static final long IP_MAX_ATTEMPTS = 30;
    private static final Duration ACCOUNT_FAILURE_WINDOW = Duration.ofMinutes(10);
    private static final Duration ACCOUNT_LOCK_DURATION = Duration.ofMinutes(10);
    private static final long ACCOUNT_FAILURE_LIMIT = 5;

    private static final DefaultRedisScript<Long> FIXED_WINDOW_INCREMENT = new DefaultRedisScript<>("""
            local count = redis.call('INCR', KEYS[1])
            if count == 1 then redis.call('PEXPIRE', KEYS[1], ARGV[1]) end
            return count
            """, Long.class);

    private static final DefaultRedisScript<Long> RECORD_ACCOUNT_FAILURE = new DefaultRedisScript<>("""
            local count = redis.call('INCR', KEYS[1])
            if count == 1 then redis.call('PEXPIRE', KEYS[1], ARGV[1]) end
            if count >= tonumber(ARGV[2]) then
              redis.call('SET', KEYS[2], '1', 'PX', ARGV[3])
            end
            return count
            """, Long.class);

    private final StringRedisTemplate redisTemplate;

    public void checkLoginIp(String ipAddress) {
        checkIpLimit("auth:rate:login:", ipAddress, ErrorCode.LOGIN_LOCKED);
    }

    public void checkRefreshIp(String ipAddress) {
        checkIpLimit("auth:rate:refresh:", ipAddress, ErrorCode.RATE_LIMITED);
    }

    public void checkAccountLock(String loginId) {
        String key = accountLockKey(loginId);
        Long ttl = redisTemplate.getExpire(key);
        if (ttl != null && ttl != -2) {
            throw new BusinessException(ErrorCode.LOGIN_LOCKED, ttl > 0 ? ttl : 600);
        }
    }

    public void recordAccountFailure(String loginId) {
        Long count = redisTemplate.execute(
                RECORD_ACCOUNT_FAILURE,
                List.of(accountFailureKey(loginId), accountLockKey(loginId)),
                String.valueOf(ACCOUNT_FAILURE_WINDOW.toMillis()),
                String.valueOf(ACCOUNT_FAILURE_LIMIT),
                String.valueOf(ACCOUNT_LOCK_DURATION.toMillis())
        );
        if (count != null && count >= ACCOUNT_FAILURE_LIMIT) {
            throw new BusinessException(ErrorCode.LOGIN_LOCKED, ACCOUNT_LOCK_DURATION.toSeconds());
        }
    }

    public void clearAccountFailures(String loginId) {
        redisTemplate.delete(accountFailureKey(loginId));
    }

    private void checkIpLimit(String keyPrefix, String ipAddress, ErrorCode errorCode) {
        String key = keyPrefix + normalizeIp(ipAddress);
        Long count = redisTemplate.execute(
                FIXED_WINDOW_INCREMENT,
                List.of(key),
                String.valueOf(IP_WINDOW.toMillis())
        );
        if (count != null && count > IP_MAX_ATTEMPTS) {
            Long ttl = redisTemplate.getExpire(key);
            long retryAfter = ttl == null || ttl < 1 ? 1 : ttl;
            throw new BusinessException(errorCode, retryAfter);
        }
    }

    private String accountFailureKey(String loginId) {
        return "auth:login:failures:" + loginId;
    }

    private String accountLockKey(String loginId) {
        return "auth:login:locked:" + loginId;
    }

    private String normalizeIp(String ipAddress) {
        return ipAddress == null || ipAddress.isBlank() ? "unknown" : ipAddress;
    }
}
