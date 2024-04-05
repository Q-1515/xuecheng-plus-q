package com.xuecheng.ucenter.feignclient;

import org.apache.ibatis.annotations.Param;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * packageName com.xuecheng.ucenter.feignclient
 *
 * @author Q
 * @version JDK 8
 * @className CheckCodeClient
 * @date 2024/4/5 18:54
 * @description 验证码校验远程调用
 */
@FeignClient(value = "checkcode", fallbackFactory = CheckCodeClientFactory.class)
public interface CheckCodeClient {


    @PostMapping(value = "/checkcode/verify")
    public Boolean verify(@RequestParam("key") String key, @RequestParam("code")String code);
}
