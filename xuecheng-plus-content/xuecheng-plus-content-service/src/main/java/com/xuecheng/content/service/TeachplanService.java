package com.xuecheng.content.service;


import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.TeachplanMedia;

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

    /***
     * @description 教学计划绑定媒资
     * @param bindTeachplanMediaDto 基本喜喜
     * @return TeachplanMedia 教学计划绑定媒资表
     * @author Q
     * @date 2024/3/31 15:19
    */
    public TeachplanMedia associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto);

    /***
     * @description 删除课程计划和媒资信息绑定
     * @param teachPlanId  课程计划id
     * @param mediaId 媒资文件id
     * @author Q
     * @date 2024/3/31 15:52
    */
    void deleteAssociationMedia(Long teachPlanId, String mediaId);
}
