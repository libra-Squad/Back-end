package com.pair.pair_backend.auth.service;

import com.pair.pair_backend.global.exception.ErrorCode;

public record RefreshResult(String accessToken, String refreshToken, ErrorCode errorCode) {
    public static RefreshResult success(String accessToken, String refreshToken) {
        return new RefreshResult(accessToken, refreshToken, null);
    }

    public static RefreshResult failure(ErrorCode errorCode) {
        return new RefreshResult(null, null, errorCode);
    }

    public boolean failed() {
        return errorCode != null;
    }
}
