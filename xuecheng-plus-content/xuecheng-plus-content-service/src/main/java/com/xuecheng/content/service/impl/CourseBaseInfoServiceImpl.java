package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * packageName com.xuecheng.content.service.impl
 *
 * @author Q
 * @version JDK 8
 * @className CourseBaseInfoServiceImpl
 * @date 2024/3/22 13:15
 * @description 课程基本信息管理业务接口实现
 */
@Slf4j
@Service
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {

    @Autowired
    private CourseBaseMapper courseBaseMapper;

    /***
     * @description 课程查询接口实现
     * @param pageParams 分页参数
     * @param queryCourseParamsDto  分页参数
     * @return PageResult<CourseBase>
     * @author Q
     * @date 2024/3/22 13:19
     */
    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        //名称模糊条件
        String courseName = queryCourseParamsDto.getCourseName();
        queryWrapper.like(StringUtils.isNotEmpty(courseName), CourseBase::getName, courseName);
        //审核状态条件
        String auditStatus = queryCourseParamsDto.getAuditStatus();
        queryWrapper.eq(StringUtils.isNotEmpty(auditStatus), CourseBase::getAuditStatus, auditStatus);
        //发布状态条件
        String publishStatus = queryCourseParamsDto.getPublishStatus();
        queryWrapper.eq(StringUtils.isNotEmpty(publishStatus), CourseBase::getStatus, publishStatus);

        //分页参数
        Long pageNo = pageParams.getPageNo();
        Long pageSize = pageParams.getPageSize();
        Page<CourseBase> page = new Page<>(pageNo, pageSize);
        Page<CourseBase> courseBasePage = courseBaseMapper.selectPage(page, queryWrapper);

        //获取数据列表和总记录数
        List<CourseBase> items = courseBasePage.getRecords();
        long total = courseBasePage.getTotal();
        //封装数据
        PageResult<CourseBase> courseBasePageResult = new PageResult<CourseBase>(items, total, pageNo, pageSize);
        return courseBasePageResult;
    }
}
