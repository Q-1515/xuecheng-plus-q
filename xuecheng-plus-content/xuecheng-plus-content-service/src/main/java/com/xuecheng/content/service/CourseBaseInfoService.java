package com.xuecheng.content.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;

/**
 * packageName com.xuecheng.content.service
 *
 * @author Q
 * @version JDK 8
 * @className CourseBaseInfoService
 * @date 2024/3/22 13:13
 * @description 课程基本信息管理业务接口
 */

public interface CourseBaseInfoService {

    /***
     * @description 课程查询接口
     * @param pageParams 分页参数
     * @param queryCourseParamsDto 查询条件
     * @return PageResult<CourseBase>
     * @author Q
     * @date 2024/3/22 13:18
     */
    PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto);

    /***
     * @description 新增课程接口
     * @param companyId  企业用户id
     * @param addCourseDto 新增课程内容
     * @return CourseBaseInfoDto 响应基础信息&营销信息
     * @author Q
     * @date 2024/3/22 19:28
     */
    CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto);
}
