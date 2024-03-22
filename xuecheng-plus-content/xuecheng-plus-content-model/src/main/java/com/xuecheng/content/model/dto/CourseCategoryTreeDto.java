package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.CourseCategory;
import lombok.Data;

import java.util.List;

/**
 * packageName com.xuecheng.content.model.dto
 *
 * @author Q
 * @version JDK 8
 * @className CourseCategoryTreeDto
 * @date 2024/3/22 15:34
 * @description 课程分类Dto
 */
@Data
public class CourseCategoryTreeDto extends CourseCategory {
    private List<CourseCategoryTreeDto> childrenTreeNodes;
}
