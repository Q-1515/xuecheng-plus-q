package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;

import java.util.List;

/**
 * packageName com.xuecheng.content.service
 *
 * @author Q
 * @version JDK 8
 * @className TeachplanService
 * @date 2024/3/25 18:18
 * @description 课程基本信息管理业务接口
 */
public interface TeachplanService {

    /***
     * @description 查询课程计划树型结构
     * @param courseId 课程id
     * @return List<TeachplanDto>
     * @author Q
     * @date 2024/3/25 18:13
     */
    public List<TeachplanDto> findTeachplayTree(long courseId);

    /***
     * @description 保存课程计划(新增/修改)
     * @param teachplanDto 课程计划信息
     * @author Q
     * @date 2024/3/25 18:59
    */
    public void saveTeachplan(SaveTeachplanDto teachplanDto);


}
