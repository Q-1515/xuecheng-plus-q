package com.xuecheng.base.execption;

/**
 * packageName com.xuecheng.base.execption
 *
 * @author Q
 * @version JDK 8
 * @className XueChengPlusException
 * @date 2024/3/25 13:44
 * @description 系统自定义异常
 */
public class XueChengPlusException extends RuntimeException {

    private String errMessage;

    public XueChengPlusException() {
    }

    public XueChengPlusException(String errMessage) {
        super(errMessage);
        this.errMessage = errMessage;
    }

    public String getErrMessage() {
        return errMessage;
    }

    //静态异常方法
    public static void cast(String errMessage){
        throw new XueChengPlusException(errMessage);
    }

    public static void cast(CommonError commonError){
        throw new XueChengPlusException(commonError.getErrMessage());
    }
}
