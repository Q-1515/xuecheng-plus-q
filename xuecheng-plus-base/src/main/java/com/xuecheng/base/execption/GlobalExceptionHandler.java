package com.xuecheng.base.execption;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

/**
 * packageName com.xuecheng.base.execption
 *
 * @author Q
 * @version JDK 8
 * @className GlobalExceptionHandler
 * @date 2024/3/25 14:16
 * @description 全局异常拦截器
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    //处理自定义异常
    @ExceptionHandler(XueChengPlusException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public RestErrorResponse doXueChengPlusException(XueChengPlusException e) {
        log.error("捕获异常信息：{}", e.getErrMessage());
        String errMessage = e.getErrMessage();
        return new RestErrorResponse(errMessage);
    }

    //jsr303异常拦截
    @ResponseBody
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse doValidException(MethodArgumentNotValidException argumentNotValidException) {
        //获取所有绑定对象
        BindingResult bindingResult = argumentNotValidException.getBindingResult();
        StringBuffer errMsg = new StringBuffer();
        //遍历添加校验异常信息
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        fieldErrors.forEach(error -> {
            errMsg.append(error.getDefaultMessage()).append(",");
        });
        log.error(errMsg.toString());
        return new RestErrorResponse(errMsg.toString());
    }


    //捕获未知异常
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public RestErrorResponse doException(Exception e) {
        log.error("捕获异常信息{}", e.getMessage());
        return new RestErrorResponse(CommonError.UNKOWN_ERROR.getErrMessage());
    }

}
