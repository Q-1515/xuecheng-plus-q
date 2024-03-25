package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.execption.XueChengPlusException;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachplanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
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

    @Autowired
    private CourseBaseMapper courseBaseMapper;

    @Autowired
    private TeachplanMediaMapper teachplanMediaMapper;

    /***
     * @description 查询课程计划树型结构
     * @param courseId 课程id
     * @return List<TeachplanDto>
     * @author Q
     * @date 2024/3/25 18:13
     */
    @Override
    public List<TeachplanDto> findTeachplayTree(long courseId) {
        List<TeachplanDto> teachplanDtos = teachplanMapper.selectTreeNodes(courseId);

        List<TeachplanDto> teachplanDtoResult = new ArrayList<>();
        HashMap<Long, TeachplanDto> teachplanDtoHashMap = new HashMap<>();

        teachplanDtos.forEach(teachplanDto -> {
            teachplanDtoHashMap.put(teachplanDto.getId(), teachplanDto);
            //获取一级目录
            if (teachplanDto.getParentid() == 0) {
                //一级目录无绑定媒资
                teachplanDto.setTeachplanMedia(null);
                teachplanDtoResult.add(teachplanDto);
            }
            //将子目录添加到父目录集合里面去
            Long parentid = teachplanDto.getParentid();
            TeachplanDto planTeachplanDto = teachplanDtoHashMap.get(parentid);
            //上级存在
            if (planTeachplanDto != null) {
                //自动创建为null的子节点集合
                List<TeachplanDto> teachplanNodes = planTeachplanDto.getTeachPlanTreeNodes();
                if (teachplanNodes == null) {
                    planTeachplanDto.setTeachPlanTreeNodes(new ArrayList<>());
                }
                planTeachplanDto.getTeachPlanTreeNodes().add(teachplanDto);
            }
        });
        return teachplanDtoResult;
    }


    /***
     * @description 保存课程计划(新增 / 修改)
     * @param teachplanDto 课程计划信息
     * @author Q
     * @date 2024/3/25 18:59
     */
    @Override
    public void saveTeachplan(SaveTeachplanDto teachplanDto) {
        //课程计划id
        Long id = teachplanDto.getId();
        Teachplan teachplan = teachplanMapper.selectById(id);
        //修改课程计划
        if (teachplan == null) {
            //取出同父同级别的课程计划数量
            int count = getTeachplanCount(teachplanDto.getCourseId(), teachplanDto.getParentid());
            Teachplan teachplanNew = new Teachplan();
            //设置排序号
            teachplanNew.setOrderby(count + 1);
            BeanUtils.copyProperties(teachplanDto, teachplanNew);
            teachplanNew.setCreateDate(LocalDateTime.now());
            teachplanMapper.insert(teachplanNew);
        } else {
            //更新课程计划信息
            BeanUtils.copyProperties(teachplanDto, teachplan);
            teachplan.setChangeDate(LocalDateTime.now());
            teachplanMapper.updateById(teachplan);
        }
    }

    /***
     * @description 删除课程计划
     * @param teachPlanId 课程计划id
     * @author Q
     * @date 2024/3/25 19:59
     */
    @Override
    public void removeTeachPlan(Long teachPlanId) {
        Teachplan teachplan = teachplanMapper.selectById(teachPlanId);
        if (teachplan == null) {
            return;
        }

        Long courseId = teachplan.getCourseId();
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (!"202002".equals(courseBase.getAuditStatus())) {
            XueChengPlusException.cast("删除失败，课程审核状态是未提交时方可删除。");
        }
        //删除一级目录
        if (teachplan.getGrade() == 1) {
            int count = getTeachplanCount(teachplan.getCourseId(), teachPlanId);
            if (count > 0) {
                throw new XueChengPlusException("课程计划信息还有子级信息，无法操作");
            }
            teachplanMapper.deleteById(teachPlanId);
        }

        //删除子目录
        teachplanMapper.deleteById(teachPlanId);
        //删除课程计划与媒资的关联信息
        teachplanMediaMapper.delete(new LambdaQueryWrapper<TeachplanMedia>().eq(TeachplanMedia::getTeachplanId, teachplan.getId()));
        //更新后面的排序字段
        updateTeachPlanOrderBy(teachplan.getParentid(), teachplan.getOrderby());
    }

    /**
     * 删除课程计划时重新排序后面字段
     *
     * @param parentid 父类id
     * @param orderBy  排序字段
     */
    public void updateTeachPlanOrderBy(Long parentid, Integer orderBy) {
        //找出同级以及排序之后的数据 && 后面排序字段数字-1
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getParentid, parentid);
        queryWrapper.gt(Teachplan::getOrderby, orderBy);
        List<Teachplan> teachplans = teachplanMapper.selectList(queryWrapper);
        if (teachplans != null && !teachplans.isEmpty()) {
            for (Teachplan teachplan : teachplans) {
                Integer orderby = teachplan.getOrderby();
                teachplan.setOrderby(orderby - 1);
                teachplanMapper.updateById(teachplan);
            }
        }
    }


    /***
     * @description 获取最新的排序号
     * @param courseId  课程id
     * @param parentId  父课程计划id
     * @return int 最新排序号
     * @author Q
     * @date 2024/3/25 19:07
     */
    private int getTeachplanCount(long courseId, long parentId) {
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId, courseId);
        queryWrapper.eq(Teachplan::getParentid, parentId);
        return teachplanMapper.selectCount(queryWrapper);
    }


}
