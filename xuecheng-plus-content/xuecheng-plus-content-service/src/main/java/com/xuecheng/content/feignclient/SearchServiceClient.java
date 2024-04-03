package com.xuecheng.content.feignclient;


import com.xuecheng.base.feignclientPo.CourseIndex;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * packageName com.xuecheng.content.feignclient
 *
 * @author Q
 * @version JDK 8
 * @className SearchServiceClient
 * @date 2024/4/3 19:06
 * @description 搜索服务远程调用
 */
@FeignClient(value = "search", fallbackFactory = SearchServiceFallbackFactory.class)
@RequestMapping("/search")
public interface SearchServiceClient {

    /***
     * @description 远程调用搜索服务添加课程索引
     * @param courseIndex 课程信息
     * @return Boolean
     * @author Q
     * @date 2024/4/3 19:23
     */
    @PostMapping("/index/course")
    public Boolean add(@RequestBody CourseIndex courseIndex);
}
