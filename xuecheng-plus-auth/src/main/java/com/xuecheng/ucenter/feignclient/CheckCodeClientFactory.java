package com.xuecheng.ucenter.feignclient;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * packageName com.xuecheng.ucenter.feignclient
 *
 * @author Q
 * @version JDK 8
 * @className CheckCodeClientFactory
 * @date 2024/4/5 18:58
 * @description 熔断降级
 */
@Slf4j
@Component
public class CheckCodeClientFactory implements FallbackFactory<CheckCodeClient> {

    /***
     * @description 验证码校验降级方法
     * @param throwable 异常信息
     * @return CheckCodeClient
     * @author Q
     * @date 2024/4/5 19:00
    */
    @Override
    public CheckCodeClient create(Throwable throwable) {
        return new CheckCodeClient(){
            @Override
            public Boolean verify(String key, String code) {
                log.error("调用验证码熔断异常：{}",throwable.getMessage());
                return false;
            }
        };
    }
}
