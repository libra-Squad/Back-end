package com.pair.pair_backend.auth.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RefreshResponse {
    private final DataDto data;

    public static RefreshResponse of(String accessToken) {
        return new RefreshResponse(new DataDto(accessToken, "Bearer", 900));
    }

    @Getter
    @RequiredArgsConstructor
    public static class DataDto {
        private final String accessToken;
        private final String tokenType;
        private final Integer expiresIn;
    }
}
