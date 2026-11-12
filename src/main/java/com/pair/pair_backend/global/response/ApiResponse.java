package com.pair.pair_backend.global.response;

import com.pair.pair_backend.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Getter
@RequiredArgsConstructor
public class ApiResponse {
    private final ErrorBody error;

    public static ApiResponse error(
            ErrorCode code,
            Map<String, String> fieldErrors,
            String requestId
    ) {
        return new ApiResponse(new ErrorBody(
                code.name(),
                code.getMessage(),
                fieldErrors,
                requestId
        ));
    }

    @Getter
    @RequiredArgsConstructor
    public static class ErrorBody {
        private final String code;
        private final String message;
        private final Map<String, String> fieldErrors;
        private final String requestId;
    }
}
