package com.qiu.cardflow.user.exception;


import cn.dev33.satoken.exception.NotLoginException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.qiu.cardflow.common.api.BaseResponse;
import com.qiu.cardflow.common.exception.ApiException;

/**
 * 全局异常处理
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    @ResponseBody
    @ExceptionHandler(value = ApiException.class)
    public BaseResponse handleBusinessException(ApiException e) {
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
    @ExceptionHandler(ConstraintViolationException.class)
    public BaseResponse handleConstraintViolationException(ConstraintViolationException ex) {
        // 提取第一个校验失败信息
        ConstraintViolation<?> violation = ex.getConstraintViolations().iterator().next();
        String errorMessage = String.format("%s",
                violation.getMessage());
        return BaseResponse.validateFailed(errorMessage);
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

    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public BaseResponse handleException(Exception e) {
        return BaseResponse.failed("服务器异常，请稍后再试");
    }
}
