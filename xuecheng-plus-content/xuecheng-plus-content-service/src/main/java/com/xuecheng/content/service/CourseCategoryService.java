package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CourseCategoryTreeDto;

import java.util.List;

/**
 * packageName com.xuecheng.content.service
 *
 * @author Q
 * @version JDK 8
 * @className CourseCategoryService
 * @date 2024/3/22 16:09
 * @description 课程分类接口
 */
public interface CourseCategoryService {
    /***
     * @description 课程分类查询
     * @param id 根节点id
     * @return List<CourseCategoryTreeDto> 返回根节点所有子节点
     * @author Q
     * @date 2024/3/22 16:11
    */
    public List<CourseCategoryTreeDto> queryTreeNodes(String id);
}
