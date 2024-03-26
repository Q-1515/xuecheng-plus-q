package com.xuecheng.media.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * packageName com.xuecheng.media.config
 *
 * @author Q
 * @version JDK 8
 * @className MinioConfig
 * @date 2024/3/26 17:36
 * @description MinIo配置类
 */
@Configuration
public class MinioConfig {

    //读取参数
    @Value("${minio.endpoint}")
    private String endpoint;
    @Value("${minio.accessKey}")
    private String accessKey;
    @Value("${minio.secretKey}")
    private String secretKey;

    @Bean
    public MinioClient minioClient() {

        MinioClient minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();

        return minioClient;
    }


}
