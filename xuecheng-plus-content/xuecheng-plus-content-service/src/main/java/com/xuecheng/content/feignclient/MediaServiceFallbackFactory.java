package com.xuecheng.content.feignclient;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * packageName com.xuecheng.content.feignclient
 *
 * @author Q
 * @version JDK 8
 * @className MediaServiceFallbackFactory
 * @date 2024/4/2 18:01
 * @description 媒资调用降级类
 */
@Slf4j
@Component
public class MediaServiceFallbackFactory implements FallbackFactory<MediaServiceClient> {


    /***
     * @description 降级后处理
     * @param throwable 异常信息
     * @return MediaServiceClient
     * @author Q
     * @date 2024/4/2 18:03
     */
    @Override
    public MediaServiceClient create(Throwable throwable) {

        /***
         * @description 远程调用媒资降级处理
         * @param throwable 异常信息
         * @return MediaServiceClient
         * @author Q
         * @date 2024/4/3 19:15
         */
        return new MediaServiceClient() {
            @Override
            public String uploadFile(MultipartFile upload, String folder, String objectName) {
                log.error("远程调用媒资上传失败熔断异常：{}", throwable.getMessage());
                return null;
            }
        };
    }
}
