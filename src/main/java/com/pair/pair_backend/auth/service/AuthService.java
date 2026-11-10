package com.pair.pair_backend.auth.service;

import com.pair.pair_backend.auth.dto.LoginRequest;
import com.pair.pair_backend.auth.dto.LoginResponse;
import com.pair.pair_backend.auth.entity.AuthSession;
import com.pair.pair_backend.auth.entity.RefreshToken;
import com.pair.pair_backend.auth.entity.User;
import com.pair.pair_backend.auth.repository.AuthSessionRepository;
import com.pair.pair_backend.auth.repository.RefreshTokenRepository;
import com.pair.pair_backend.auth.repository.UserRepository;
import com.pair.pair_backend.global.exception.BusinessException;
import com.pair.pair_backend.global.exception.ErrorCode;
import com.pair.pair_backend.global.security.JwtTokenProvider;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AuthService {
    private static final Pattern LOGIN_ID_PATTERN = Pattern.compile("^[a-z][a-z0-9_]{4,19}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?!.*\\s)(?:(?=.*[a-zA-Z])(?=.*\\d)|(?=.*[a-zA-Z])(?=.*[\\W_])|(?=.*\\d)(?=.*[\\W_]))[a-zA-Z\\d\\W_]{8,64}$");
    private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(14);

    private final UserRepository userRepository;
    private final AuthSessionRepository authSessionRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthRateLimitService authRateLimitService;
    private final EntityManagerFactory entityManagerFactory;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public LoginResult login(LoginRequest request, String ipAddress) {
        authRateLimitService.checkLoginIp(ipAddress);
        if (request == null || isBlank(request.getLoginId()) || isBlank(request.getPassword())) {
            throw new BusinessException(ErrorCode.INPUT_INVALID);
        }

        String loginId = request.getLoginId().trim();
        if (!LOGIN_ID_PATTERN.matcher(loginId).matches()
                || !PASSWORD_PATTERN.matcher(request.getPassword()).matches()) {
            throw new BusinessException(ErrorCode.INPUT_INVALID);
        }
        authRateLimitService.checkAccountLock(loginId);

        User user = userRepository.findByLoginId(loginId).orElse(null);
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            authRateLimitService.recordAccountFailure(loginId);
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        authRateLimitService.clearAccountFailures(loginId);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plus(REFRESH_TOKEN_TTL);
        AuthSession session = authSessionRepository.save(AuthSession.create(user, now, expiresAt));

        String rawRefreshToken = createRefreshToken();
        refreshTokenRepository.save(RefreshToken.issue(
                session, hashRefreshToken(rawRefreshToken), now, expiresAt));

        String accessToken = jwtTokenProvider.createAccessToken(user.getUserId());
        LoginResponse response = LoginResponse.from(accessToken, user, companyId(user));
        return new LoginResult(response, rawRefreshToken);
    }

    @Transactional
    public RefreshResult refresh(String rawRefreshToken, String ipAddress) {
        authRateLimitService.checkRefreshIp(ipAddress);
        if (isBlank(rawRefreshToken)) {
            return RefreshResult.failure(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        LocalDateTime now = LocalDateTime.now();
        RefreshToken storedToken = refreshTokenRepository
                .findByTokenHashForUpdate(hashRefreshToken(rawRefreshToken))
                .orElse(null);
        if (storedToken == null) {
            return RefreshResult.failure(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        AuthSession session = storedToken.getSession();
        if (storedToken.getUsedAt() != null) {
            session.revoke(now);
            refreshTokenRepository.revokeUnusedForSession(session.getSessionId(), now);
            return RefreshResult.failure(ErrorCode.REFRESH_TOKEN_REUSED);
        }
        if (!storedToken.isUsable(now) || !session.isActive(now)) {
            return RefreshResult.failure(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        storedToken.markUsed(now);
        LocalDateTime nextExpiresAt = now.plus(REFRESH_TOKEN_TTL);
        session.extendUntil(nextExpiresAt);
        String nextRawToken = createRefreshToken();
        refreshTokenRepository.save(RefreshToken.issue(
                session, hashRefreshToken(nextRawToken), now, nextExpiresAt));

        String accessToken = jwtTokenProvider.createAccessToken(session.getUser().getUserId());
        return RefreshResult.success(accessToken, nextRawToken);
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        if (isBlank(rawRefreshToken)) {
            return;
        }

        RefreshToken storedToken = refreshTokenRepository
                .findByTokenHashForUpdate(hashRefreshToken(rawRefreshToken))
                .orElse(null);
        if (storedToken == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        AuthSession session = storedToken.getSession();
        session.revoke(now);
        refreshTokenRepository.revokeUnusedForSession(session.getSessionId(), now);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String companyId(User user) {
        if (user.getCompanyId() == null) {
            return null;
        }
        Object id = entityManagerFactory.getPersistenceUnitUtil()
                .getIdentifier(user.getCompanyId());
        return id == null ? null : id.toString();
    }

    private String createRefreshToken() {
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    private String hashRefreshToken(String rawRefreshToken) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(rawRefreshToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }
}
