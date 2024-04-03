package com.xuecheng.content.feignclient;

import com.xuecheng.base.feignclientPo.CourseIndex;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.lang.annotation.Annotation;

/**
 * packageName com.xuecheng.content.feignclient
 *
 * @author Q
 * @version JDK 8
 * @className SearchServiceFallbackFactory
 * @date 2024/4/3 19:12
 * @description 搜索服务远程调用降级类
 */
@Slf4j
@Component
public class SearchServiceFallbackFactory implements FallbackFactory<SearchServiceClient> {


    /***
     * @description 远程调用搜索服务
     * @param throwable 异常信息
     * @return SearchServiceClient
     * @author Q
     * @date 2024/4/3 19:15
     */
    @Override
    public SearchServiceClient create(Throwable throwable) {
        return new SearchServiceClient() {
            @Override
            public Boolean add(CourseIndex courseIndex) {
                throwable.printStackTrace();
                log.error("远程调用媒资上传失败熔断异常：{}", throwable.getMessage());
                return false;
            }
        };
    }
}
