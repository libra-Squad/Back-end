package com.pair.pair_backend.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    INPUT_INVALID(HttpStatus.BAD_REQUEST, "입력값 누락 또는 형식이 잘못되었습니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "아이디/비밀번호 불일치 또는 접근 불가 계정입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 refresh token입니다."),
    REFRESH_TOKEN_REUSED(HttpStatus.UNAUTHORIZED, "회전 전에 사용된 refresh token이 재사용되었습니다."),
    INVALID_ORIGIN(HttpStatus.FORBIDDEN, "허용되지 않은 요청 출처입니다."),
    RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS, "요청 제한을 초과했습니다."),
    LOGIN_LOCKED(HttpStatus.TOO_MANY_REQUESTS, "로그인 실패 횟수 또는 속도 제한이 초과되었습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "요청 권한이 없습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "요청을 완료하지 못했습니다. request_id로 추적합니다.");

    private final HttpStatus status;
    private final String message;

}
