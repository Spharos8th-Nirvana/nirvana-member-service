package com.nirvana.member.common.web;

public record ApiResponse<T>(boolean success, T data, ApiError error) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static ApiResponse<Void> failure(String code, String message, String traceId) {
        return new ApiResponse<>(false, null, new ApiError(code, message, traceId));
    }

    public record ApiError(String code, String message, String traceId) {
    }
}
