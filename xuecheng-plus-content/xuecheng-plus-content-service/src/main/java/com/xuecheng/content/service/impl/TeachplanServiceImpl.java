package com.xuecheng.content.service.impl;

import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.model.dto.TeachplanDto;
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
 * @className TeachplanServiceImpl
 * @date 2024/3/25 18:12
 * @description 课程基本信息管理业务接口
 */
@Slf4j
@Service
public class TeachplanServiceImpl implements TeachplanService {

    @Autowired
    private TeachplanMapper teachplanMapper;

    /***
     * @description 查询课程计划树型结构
     * @param courseId 课程id
     * @return List<TeachplanDto>
     * @author Q
     * @date 2024/3/25 18:13
     */
    @Override
    public List<TeachplanDto> findTeachplayTree(long courseId) {
        return teachplanMapper.selectTreeNodes(courseId);
    }
}
