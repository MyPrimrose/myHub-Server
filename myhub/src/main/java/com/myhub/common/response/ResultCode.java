package com.myhub.common.response;

public enum ResultCode {
    SUCCESS(200, "success"), BAD_REQUEST(400, "请求参数错误"), INTERNAL_ERROR(500, "服务器内部错误"), DATABASE_UNAVAILABLE(503, "数据库暂不可用");
    private final int code;
    private final String message;
    ResultCode(int code, String message) { this.code = code; this.message = message; }
    public int getCode() { return code; }
    public String getMessage() { return message; }
}
