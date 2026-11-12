package com.pair.pair_backend.global.exception;


import lombok.Getter;

public class BusinessException extends RuntimeException {
    @Getter
    private final ErrorCode errorCode;

    @Getter
    private final Long retryAfterSeconds;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.retryAfterSeconds = null;
    }

    public BusinessException(ErrorCode errorCode, long retryAfterSeconds) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.retryAfterSeconds = retryAfterSeconds;
    }
}
