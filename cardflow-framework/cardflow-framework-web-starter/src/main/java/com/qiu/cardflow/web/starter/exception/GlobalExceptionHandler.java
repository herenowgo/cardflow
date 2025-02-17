package com.qiu.cardflow.web.starter.exception;

import cn.dev33.satoken.exception.NotLoginException;
import com.qiu.cardflow.common.interfaces.api.BaseResponse;
import com.qiu.cardflow.common.interfaces.exception.BusinessException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 全局异常处理
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    @ResponseBody
    @ExceptionHandler(value = BusinessException.class)
    public BaseResponse handleValidException(BusinessException e) {
        String message = e.getMessage();
        return BaseResponse.failed(message);
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
    @ExceptionHandler(value = ConstraintViolationException.class)
    public BaseResponse handleConstraintViolationExceptions(ConstraintViolationException e) {
        String message = e.getMessage();
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
