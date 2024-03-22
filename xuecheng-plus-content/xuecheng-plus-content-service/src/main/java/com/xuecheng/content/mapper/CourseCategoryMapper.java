package com.xuecheng.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.model.po.CourseCategory;

import java.util.List;

/**
 * <p>
 * 课程分类 Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface CourseCategoryMapper extends BaseMapper<CourseCategory> {

    /***
     * @description 课程分类树形查询
     * @param id 根节点id
     * @return List<CourseCategoryTreeDto>
     * @author Q
     * @date 2024/3/22 15:50
     */
    List<CourseCategoryTreeDto> selectTreeNodes(String id);

}
