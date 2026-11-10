package com.pair.pair_backend.auth.service;

import com.pair.pair_backend.auth.dto.LoginResponse;

/** Internal result. The refresh token is written only to the HttpOnly cookie. */
public record LoginResult(LoginResponse response, String refreshToken) {
}
