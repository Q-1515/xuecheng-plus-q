package com.xuecheng.content.service.impl;

import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.service.TeachplanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * packageName com.xuecheng.content.service.impl
 *
 * @author Q
 * @version JDK 8
 * @className CoursePublishServiceImpl
 * @date 2024/4/1 13:25
 * @description 课程预览发布接口
 */
@Slf4j
@Service
public class CoursePublishServiceImpl implements CoursePublishService {

    @Autowired
    private CourseBaseInfoService courseBaseInfoService;

    @Autowired
    private TeachplanService teachplanService;


    /***
     * @description 获取课程预览信息
     * @param courseId 课程id
     * @return CoursePreviewDto
     * @author Q
     * @date 2024/4/1 13:25
     */
    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {

        //基本信息&营销新
        CourseBaseInfoDto courseBase = courseBaseInfoService.getCourseBase(courseId);
        //教学计划
        List<TeachplanDto> teachplayTree = teachplanService.findTeachplayTree(courseId);
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        coursePreviewDto.setCourseBase(courseBase);
        coursePreviewDto.setTeachplans(teachplayTree);
        return coursePreviewDto;
    }
}
