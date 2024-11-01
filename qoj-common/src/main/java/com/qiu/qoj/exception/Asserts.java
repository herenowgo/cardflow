package com.qiu.qoj.exception;


import com.qiu.qoj.domain.IErrorCode;

/**
 * 断言处理类，用于抛出各种API异常
 * Created by macro on 2020/2/27.
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
