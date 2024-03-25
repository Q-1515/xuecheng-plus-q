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
import com.xuecheng.content.service.CourseMarketService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    @Autowired
    private CourseMarketMapper courseMarketMapper;

    @Autowired
    private CourseCategoryMapper courseCategoryMapper;

    @Autowired
    private CourseMarketService courseMarketService;

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

    /***
     * @description 新增课程接口
     * @param companyId  企业用户id
     * @param addCourseDto 新增课程内容
     * @return CourseBaseInfoDto 响应基础信息&营销信息
     * @author Q
     * @date 2024/3/22 19:28
     */
    @Transactional
    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto) {
        //数据封装插入数据
        //课程基本信息插入
        CourseBase courseBase = new CourseBase();
        //设置机构id
        courseBase.setCompanyId(companyId);
        courseBase.setCreateDate(LocalDateTime.now());
        //审核状态未提交
        courseBase.setAuditStatus("202002");
        //发布状态未发布
        courseBase.setStatus("203001");
        BeanUtils.copyProperties(addCourseDto, courseBase);
        int insert = courseBaseMapper.insert(courseBase);

        //课程营销信息插入
        //获取课程id
        Long id = courseBase.getId();
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(addCourseDto, courseMarket);
        courseMarket.setId(id);
        //营销信息保存
        int insert1 = this.saveCourseMarket(courseMarket);
        if (insert <= 0 || insert1 <= 0) {
            throw new RuntimeException("添加课程失败");
        }
        CourseBaseInfoDto courseBaseInfo = getCourseBaseInfo(id);
        return courseBaseInfo;
    }


    //根据课程id查询课程基本信息，包括基本信息和营销信息
    public CourseBaseInfoDto getCourseBaseInfo(Long courseId) {
        //查询课程基本信息
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        //查询课程营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        //拷贝信息
        BeanUtils.copyProperties(courseBase, courseBaseInfoDto);
        BeanUtils.copyProperties(courseMarket, courseBaseInfoDto);
        //查询分类名称
        //st名字
        CourseCategory courseCategoryBySt = courseCategoryMapper.selectById(courseBase.getSt());
        courseBaseInfoDto.setStName(courseCategoryBySt.getName());
        //mt名字
        CourseCategory courseCategoryByMt = courseCategoryMapper.selectById(courseBase.getMt());
        courseBaseInfoDto.setMtName(courseCategoryByMt.getName());
        return courseBaseInfoDto;
    }


    /***
     * @description 修改课程
     * @param companyId 组织id
     * @param dto 修改信息
     * @return CourseBaseInfoDto
     * @author Q
     * @date 2024/3/25 16:10
     */
    @Transactional
    @Override
    public CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto dto) {
        //课程id
        Long courseId = dto.getId();
        CourseBase courseBaseUpdate = courseBaseMapper.selectById(courseId);
        if (courseBaseUpdate == null) {
            XueChengPlusException.cast("课程不存在");
        }
        if (!companyId.equals(courseBaseUpdate.getCompanyId())) {
            XueChengPlusException.cast("只允许修改本机构的课程");
        }

        //封装基本信息数据
        BeanUtils.copyProperties(dto, courseBaseUpdate);
        courseBaseUpdate.setChangeDate(LocalDateTime.now());
        int i = courseBaseMapper.updateById(courseBaseUpdate);

        //封装营销信息数据
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(dto, courseMarket);
        int b = this.saveCourseMarket(courseMarket);
        if (i <= 0 || b <= 0) {
            throw new RuntimeException("修改课程失败");
        }
        //查询课程信息
        return this.getCourseBaseInfo(courseId);
    }

    //营销信息保存
    private int saveCourseMarket(CourseMarket courseMarket){
        String charge = courseMarket.getCharge();
        if(StringUtils.isBlank(charge)){
            XueChengPlusException.cast("请设置收费规则");
        }
        if(charge.equals("201001")){
            Float price = courseMarket.getPrice();
            if(price == null || price.floatValue()<=0){
                XueChengPlusException.cast("课程设置了收费价格不能为空且必须大于0");
            }
        }
        boolean b = courseMarketService.saveOrUpdate(courseMarket);
        return b?1:-1;
    }
}
