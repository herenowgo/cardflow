package com.qiu.cardflow.common.exception;

import cn.dev33.satoken.exception.NotLoginException;
import com.qiu.cardflow.common.api.BaseResponse;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 全局异常处理
 * Created by macro on 2020/2/27.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ResponseBody
    @ExceptionHandler(value = ApiException.class)
    public BaseResponse handle(ApiException e) {
        if (e.getErrorCode() != null) {
            return BaseResponse.failed(e.getErrorCode());
        }
        return BaseResponse.failed(e.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public BaseResponse handleValidException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        String message = null;
        if (bindingResult.hasErrors()) {
            FieldError fieldError = bindingResult.getFieldError();
            if (fieldError != null) {
                message = fieldError.getField() + fieldError.getDefaultMessage();
            }
        }
        return BaseResponse.validateFailed(message);
    }

    @ResponseBody
    @ExceptionHandler(value = BindException.class)
    public BaseResponse handleValidException(BindException e) {
        BindingResult bindingResult = e.getBindingResult();
        String message = null;
        if (bindingResult.hasErrors()) {
            FieldError fieldError = bindingResult.getFieldError();
            if (fieldError != null) {
                message = fieldError.getField() + fieldError.getDefaultMessage();
            }
        }
        return BaseResponse.validateFailed(message);
    }


    @ResponseBody
    @ExceptionHandler(value = NotLoginException.class)
    public BaseResponse handleValidException(NotLoginException e) {

        return BaseResponse.failed(e.getMessage());
    }
}
