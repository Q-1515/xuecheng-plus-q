package com.xuecheng.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.utils.StringUtils;
import com.xuecheng.base.execption.XueChengPlusException;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.mapper.CoursePublishPreMapper;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.model.po.CoursePublishPre;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.service.TeachplanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * packageName com.xuecheng.content.service.impl
 *
 * @author Q
 * @version JDK 8
 * @className CoursePublishServiceImpl
 * @date 2024/4/1 13:25
 * @description 课程预览发布接口
 */
@Slf4j
@Service
public class CoursePublishServiceImpl implements CoursePublishService {

    @Autowired
    private CourseBaseInfoService courseBaseInfoService;

    @Autowired
    private TeachplanService teachplanService;

    @Autowired
    private CourseBaseMapper courseBaseMapper;

    @Autowired
    private CourseMarketMapper courseMarketMapper;

    @Autowired
    private CoursePublishPreMapper coursePublishPreMapper;


    /***
     * @description 获取课程预览信息
     * @param courseId 课程id
     * @return CoursePreviewDto
     * @author Q
     * @date 2024/4/1 13:25
     */
    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {

        //基本信息&营销新
        CourseBaseInfoDto courseBase = courseBaseInfoService.getCourseBase(courseId);
        //教学计划
        List<TeachplanDto> teachplayTree = teachplanService.findTeachplayTree(courseId);
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        coursePreviewDto.setCourseBase(courseBase);
        coursePreviewDto.setTeachplans(teachplayTree);
        return coursePreviewDto;
    }

    /***
     * @description 提交审核
     * @param companyId 机构id
     * @param courseId 课程id
     * @author Q
     * @date 2024/4/2 11:11
     */
    @Override
    public void commitAudit(Long companyId, Long courseId) {
        //约束校验
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null){
            XueChengPlusException.cast("课程不存在，请重试");
        }
        //课程审核状态
        String auditStatus = courseBase.getAuditStatus();
        //当前审核状态为已提交不允许再次提交
        if("202003".equals(auditStatus)){
            XueChengPlusException.cast("当前为等待审核状态，审核完成可以再次提交。");
        }
        //本机构只允许提交本机构的课程
        if(!courseBase.getCompanyId().equals(companyId)){
            XueChengPlusException.cast("不允许提交其它机构的课程。");
        }
        //课程图片是否填写
        if(StringUtils.isEmpty(courseBase.getPic())){
            XueChengPlusException.cast("提交失败，请上传课程图片");
        }
        //查询课程计划信息
        List<TeachplanDto> teachplanTree = teachplanService.findTeachplayTree(courseId);
        if(teachplanTree.isEmpty()){
            XueChengPlusException.cast("提交失败，还没有添加课程计划");
        }

        //封装数据、基本表信息、营销信息、课程计划、师资信息
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        //进本信息营销信息
        CourseBaseInfoDto courseBaseInfoDto = courseBaseInfoService.getCourseBase(courseId);
        BeanUtils.copyProperties(courseBaseInfoDto,coursePublishPre);
        //课程营销信息转为json
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        String courseMarketJson = JSON.toJSONString(courseMarket);
        //将课程营销信息json数据放入课程预发布表
        coursePublishPre.setMarket(courseMarketJson);

        //添加课程计划
        String  teachplanTreeJson = JSON.toJSONString(teachplanTree);
        coursePublishPre.setTeachplan(teachplanTreeJson);

        //设置预发布记录状态,已提交
        coursePublishPre.setStatus("202003");
        //教学机构id
        coursePublishPre.setCompanyId(companyId);
        //提交时间
        coursePublishPre.setCreateDate(LocalDateTime.now());
        CoursePublishPre coursePublishPreUpdate = coursePublishPreMapper.selectById(courseId);
        if(coursePublishPreUpdate == null){
            //添加课程预发布记录
            coursePublishPreMapper.insert(coursePublishPre);
        }else{
            coursePublishPreMapper.updateById(coursePublishPre);
        }
        //更新课程基本表的审核状态
        courseBase.setAuditStatus("202003");
        courseBaseMapper.updateById(courseBase);
    }
}
