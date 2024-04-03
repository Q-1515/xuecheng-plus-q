package com.xuecheng.content;

import com.spring4all.swagger.EnableSwagger2Doc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = {"com.xuecheng.content.feignclient"})
@EnableSwagger2Doc
@SpringBootApplication(scanBasePackages = {"com.xuecheng.base","com.xuecheng.content","com.xuecheng.messagesdk"})
public class ContentApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContentApiApplication.class, args);
    }

}
