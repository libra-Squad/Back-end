package com.pair.pair_backend.auth.service;

import com.pair.pair_backend.auth.dto.LoginResponse;

/** Internal result. The refresh token is written only to the HttpOnly cookie. 라는 뜻은
 * 저도 잘 모르겠지만 대충 영원한 결과고, 리프레시 토큰은 오직 HttpOnly cookie에 의해 작성된다 라는거 같은디*/
public record LoginResult(LoginResponse response, String refreshToken) {
}
