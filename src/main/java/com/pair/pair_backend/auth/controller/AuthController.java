package com.pair.pair_backend.auth.controller;

import com.pair.pair_backend.auth.dto.LoginRequest;
import com.pair.pair_backend.auth.dto.LoginResponse;
import com.pair.pair_backend.auth.dto.RefreshResponse;
import com.pair.pair_backend.auth.service.AuthService;
import com.pair.pair_backend.auth.service.LoginResult;
import com.pair.pair_backend.auth.service.RefreshResult;
import com.pair.pair_backend.global.exception.BusinessException;
import com.pair.pair_backend.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @Value("${app.cors.allowed-origins:http://localhost:3000}")
    private List<String> allowedOrigins;

    @Valid
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        LoginResult result = authService.login(loginRequest, request.getRemoteAddr());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie(result.refreshToken()).toString())
                .body(result.response());
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            @RequestHeader(value = HttpHeaders.ORIGIN, required = false) String origin,
            HttpServletRequest request) {
        if (origin == null || !allowedOrigins.contains(origin)) {
            throw new BusinessException(ErrorCode.INVALID_ORIGIN);
        }

        RefreshResult result = authService.refresh(refreshToken, request.getRemoteAddr());
        if (result.failed()) {
            throw new BusinessException(result.errorCode());
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie(result.refreshToken()).toString())
                .body(RefreshResponse.of(result.accessToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            @RequestHeader(value = HttpHeaders.ORIGIN, required = false) String origin) {

        if (origin == null || !allowedOrigins.contains(origin)) {
            throw new BusinessException(ErrorCode.INVALID_ORIGIN);
        }

        authService.logout(refreshToken);

        ResponseCookie expiredCookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/api/v1/auth")
                .maxAge(Duration.ZERO)
                .build();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, expiredCookie.toString())
                .build();
    }

    private ResponseCookie refreshCookie(String refreshToken) {
        return ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/api/v1/auth")
                .maxAge(Duration.ofDays(14))
                .build();
    }
}
