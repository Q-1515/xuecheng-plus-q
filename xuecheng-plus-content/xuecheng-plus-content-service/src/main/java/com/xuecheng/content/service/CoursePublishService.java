package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CoursePreviewDto;

import java.io.File;

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


    /***
     * @description 提交审核
     * @param companyId 机构id
     * @param courseId 课程id
     * @author Q
     * @date 2024/4/2 11:11
     */
    public void commitAudit(Long companyId, Long courseId);

    /***
     * @description 课程发布接口
     * @param companyId 机构id
     * @param courseId 课程id
     * @author Q
     * @date 2024/4/2 14:53
    */
    public void publish(Long companyId,Long courseId);


    /**
     * @description 课程静态化
     * @param courseId  课程id
     * @return File 静态化文件
     * @author Mr.M
     * @date 2022/9/23 16:59
     */
    public File generateCourseHtml(Long courseId);

    /**
     * @description 上传课程静态化页面
     * @param file  静态化文件
     * @return void
     * @author Mr.M
     * @date 2022/9/23 16:59
     */
    public void  uploadCourseHtml(Long courseId,File file);



    /***
     * @description 远程调用搜索服务保存索引
     * @param courseId 课程id
     * @return Boolean
     * @author Q
     * @date 2024/4/3 19:52
    */
    public Boolean saveCourseIndex(Long courseId);


}
