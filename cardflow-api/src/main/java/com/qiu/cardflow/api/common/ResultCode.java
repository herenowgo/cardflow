package com.qiu.cardflow.api.common;


import lombok.Getter;

/**
 * 枚举了一些常用API操作码
 */
@Getter
public enum ResultCode implements IErrorCode {
    SUCCESS(200, "ok"),
    FAILED(500, "fail"),
    VALIDATE_FAILED(404, "参数检验失败"),
    UNAUTHORIZED(401, "暂未登录或token已经过期"),
    FORBIDDEN(403, "没有相关权限");
    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
