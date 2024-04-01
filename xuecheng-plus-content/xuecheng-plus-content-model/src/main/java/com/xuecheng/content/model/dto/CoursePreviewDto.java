package com.xuecheng.content.model.dto;

import lombok.Data;

import java.util.List;

/**
 * packageName com.xuecheng.content.model.dto
 *
 * @author Q
 * @version JDK 8
 * @className CoursePreviewDto
 * @date 2024/4/1 13:22
 * @description 课程预览模型
 */
@Data
public class CoursePreviewDto {

    //课程基本信息,课程营销信息
    CourseBaseInfoDto courseBase;


    //课程计划信息
    List<TeachplanDto> teachplans;

    //师资信息暂时不加...

}
