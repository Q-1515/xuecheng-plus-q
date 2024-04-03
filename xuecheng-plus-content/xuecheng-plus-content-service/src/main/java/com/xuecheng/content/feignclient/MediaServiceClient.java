package com.xuecheng.content.feignclient;

import com.xuecheng.content.config.MultipartSupportConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

/**
 * packageName com.xuecheng.content.feignclient
 *
 * @author Q
 * @version JDK 8
 * @className MediaServiceClient
 * @date 2024/4/2 16:39
 * @description 媒资管理远程调用
 */
@FeignClient(value = "media-api", configuration = MultipartSupportConfig.class, fallbackFactory = MediaServiceFallbackFactory.class)
public interface MediaServiceClient {

    @RequestMapping(value = "/media/upload/coursefile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String uploadFile(@RequestPart("filedata") MultipartFile upload, @RequestParam(value = "folder", required = false) String folder, @RequestParam(value = "objectName", required = false) String objectName);

}
