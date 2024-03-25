package com.xuecheng.base.execption;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

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


    //捕获未知异常
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public RestErrorResponse doException(Exception e) {
        log.error("捕获异常信息{}", e.getMessage());
        return new RestErrorResponse(CommonError.UNKOWN_ERROR.getErrMessage());
    }

}
