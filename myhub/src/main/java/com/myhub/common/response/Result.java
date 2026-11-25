package com.myhub.common.response;

public record Result<T>(int code, String message, T data, long timestamp) {
    public static <T> Result<T> success(T data) { return new Result<>(200, "success", data, System.currentTimeMillis()); }
    public static Result<Void> failure(ResultCode code) { return new Result<>(code.getCode(), code.getMessage(), null, System.currentTimeMillis()); }
    public static Result<Void> failure(int code, String message) { return new Result<>(code, message, null, System.currentTimeMillis()); }
}
