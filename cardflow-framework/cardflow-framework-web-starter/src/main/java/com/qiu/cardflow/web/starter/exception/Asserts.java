package com.qiu.cardflow.web.starter.exception;


import com.qiu.cardflow.web.starter.constants.IErrorCode;

/**
 * 断言处理类，用于抛出各种API异常
 */
public class Asserts {
    public static void fail(String message) {
        throw new ApiException(message);
    }

    public static void failIf(Boolean condition, String message) {
        if (condition) {
            throw new ApiException(message);
        }
    }

    public static void fail(IErrorCode errorCode) {
        throw new ApiException(errorCode);
    }

    public static void failIf(Boolean condition, IErrorCode errorCode) {
        if (condition) {
            throw new ApiException(errorCode);
        }
    }
}
