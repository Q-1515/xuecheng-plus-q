package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CourseCategoryTreeDto;

import java.util.List;

/**
 * 课程分类 Service
 *
 * @author QLJ
 * @date 2023-2-20 0020 19:55
 */
public interface CourseCategoryService {

    /**
     * 查询课程分类
     * @param id 根节点id
     * @return 课程分类
     */
    public List<CourseCategoryTreeDto> queryTreeNodes(String id);

}
