package com.xuecheng.content.service;


import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;

import java.util.List;

/**
 * 课程计划 业务层
 *
 * @author QLJ
 * @date 2023-2-27 0027 17:23
 */
public interface TeachplanService {

    /**
     * 查询课程树形列表
     * @param courseId 课程id
     * @return 课程计划
     */
    public List<TeachplanDto> findTeachplayTree(Long courseId);

    /**
     * 课程计划创建或修改
     * @param saveTeachplanDto 课程计划创建修改实体
     */
    public void teachplanService(SaveTeachplanDto saveTeachplanDto);

    /**
     * 删除课程计划
     * @param teachPlanId 课程计划id
     */
    void removeTeachPlan(Long teachPlanId);

    /**
     * 移动课程计划
     * @param moveType 移动类型
     * @param teachPlanId 课程计划id
     */
    void moveTeachPlan(String moveType, Long teachPlanId);
}
