package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.execption.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.service.CourseBaseInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 课程管理 业务层实现类
 *
 * @author QLJ
 * @date 2023-2-17 0017 21:02
 */
@Service
@Slf4j
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {

    @Autowired
    private CourseBaseMapper courseBaseMapper;

    @Autowired
    private CourseMarketMapper courseMarketMapper;

    @Autowired
    private CourseCategoryMapper courseCategoryMapper;

    @Autowired
    private CourseMarketServiceImpl courseMarketService;


    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {

        LambdaQueryWrapper<CourseBase> courseBaseLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //课程名称条件
        courseBaseLambdaQueryWrapper.like(StringUtils.isNotEmpty(queryCourseParamsDto.getCourseName()), CourseBase::getName, queryCourseParamsDto.getCourseName());
        //审核状态条件
        courseBaseLambdaQueryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getAuditStatus()), CourseBase::getAuditStatus, queryCourseParamsDto.getAuditStatus());
        //发布状态条件
        courseBaseLambdaQueryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getPublishStatus()), CourseBase::getStatus, queryCourseParamsDto.getPublishStatus());
        courseBaseLambdaQueryWrapper.eq(CourseBase::getCompanyId,queryCourseParamsDto.getCompanyId());

        //设置分页参数
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        Page<CourseBase> courseBasePage = courseBaseMapper.selectPage(page, courseBaseLambdaQueryWrapper);
        //数据列表
        List<CourseBase> courseBaseList = courseBasePage.getRecords();
        //总记录数
        long total = courseBasePage.getTotal();

        return new PageResult<>(courseBaseList, total, pageParams.getPageNo(), pageParams.getPageSize());
    }

    /**
     * @param companyId    教学机构id
     * @param dto 课程基本信息
     * @return com.xuecheng.content.model.dto.CourseBaseInfoDto
     * @description 添加课程信息
     * @author QLJ
     * @date 2023-2-21
     */
    @Override
    @Transactional
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto dto) {
        CourseBase courseBase = new CourseBase();
        BeanUtils.copyProperties(dto, courseBase);
        //设置机构id
        courseBase.setCompanyId(companyId);
        //创建时间
        courseBase.setCreateDate(LocalDateTime.now());
        //设置审核状态为未提交
        courseBase.setAuditStatus("202002");
        //发布状态为未发布
        courseBase.setStatus("2003001");


        //课程基本信息表插入
        int insert = courseBaseMapper.insert(courseBase);
        //获取课程id
        Long courseId = courseBase.getId();
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(dto, courseMarket);
        courseMarket.setId(courseId);

        //课程营销表插入
        int insert1 = courseMarketService.saveCourseMarket(courseMarket);

        if (insert <= 0 || insert1 <= 0) {
            XueChengPlusException.cast("添加课程信息失败");
        }

        //组装返回接口
        CourseBaseInfoDto courseBaseInfo = getCourseBase(courseId);
        return courseBaseInfo;
    }

    /**
     * 根据课程id查询课程基本和营销信息
     *
     * @param courseId 课程id
     * @return 课程基本和营销信息
     */
    public CourseBaseInfoDto getCourseBase(Long courseId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);

        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase, courseBaseInfoDto);
        if (courseMarket != null) {
            BeanUtils.copyProperties(courseMarket, courseBaseInfoDto);
        }

        //课程分类名称
        //大小分类编号
        String mt = courseBase.getMt();
        String st = courseBase.getSt();

        CourseCategory mtInfo = courseCategoryMapper.selectById(mt);
        CourseCategory stInfo = courseCategoryMapper.selectById(st);
        if (mtInfo != null) {
            courseBaseInfoDto.setMtName(mtInfo.getName());
        }
        if (stInfo != null) {
            courseBaseInfoDto.setStName(stInfo.getName());
        }
        return courseBaseInfoDto;
    }

    /**
     * 修改课程信息
     *
     * @param companyId 机构id
     * @param dto       课程信息
     * @return 课程基本和营销信息
     */
    @Override
    @Transactional
    public CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto dto) {
        CourseBase courseBase = courseBaseMapper.selectById(dto.getId());
        if (courseBase == null) {
            XueChengPlusException.cast("课程信息不存在");
        }
        //机构校验
        if (!courseBase.getCompanyId().equals(companyId)) {
            XueChengPlusException.cast("只允许修改本机构课程");
        }

        //封装基本信息数据
        BeanUtils.copyProperties(dto, courseBase);
        courseBase.setChangeDate(LocalDateTime.now());

        int i = courseBaseMapper.updateById(courseBase);

        //修改营销表数据
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(dto, courseMarket);

        int b = courseMarketService.saveCourseMarket(courseMarket);
        if (i <= 0 || b<=0) {
            XueChengPlusException.cast("更新课程表失败");
        }
        return this.getCourseBase(courseBase.getId());
    }
}
