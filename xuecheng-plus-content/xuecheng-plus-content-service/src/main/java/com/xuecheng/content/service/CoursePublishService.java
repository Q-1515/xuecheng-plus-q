package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CoursePreviewDto;

/**
 * packageName com.xuecheng.content.service
 *
 * @author Q
 * @version JDK 8
 * @className CoursePublishService
 * @date 2024/4/1 13:24
 * @description 课程预览发布接口
 */
public interface CoursePublishService {

    /***
     * @description 获取课程预览信息
     * @param courseId 课程id
     * @return CoursePreviewDto
     * @author Q
     * @date 2024/4/1 13:25
    */
    public CoursePreviewDto getCoursePreviewInfo(Long courseId);
}
