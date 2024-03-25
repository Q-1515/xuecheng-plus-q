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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * 课程计划 业务实现类
 *
 * @author QLJ
 * @date 2023-2-27 0027 17:25
 */
@Service
@Slf4j
public class TeachplanServiceImpl implements TeachplanService {

    @Autowired
    private TeachplanMapper teachplanMapper;

    @Autowired
    private CourseBaseMapper courseBaseMapper;

    @Autowired
    private TeachplanMediaMapper teachplanMediaMapper;

    @Override
    public List<TeachplanDto> findTeachplayTree(Long courseId) {
        List<TeachplanDto> teachplanDtos = teachplanMapper.selectTreeNodes(courseId);

        List<TeachplanDto> teachplanDtoResult = new ArrayList<>();
        HashMap<Long, TeachplanDto> teachplanDtoHashMap = new HashMap<>();

        teachplanDtos.forEach(teachplanDto -> {
            teachplanDtoHashMap.put(teachplanDto.getId(), teachplanDto);
            //获取一级目录
            if (teachplanDto.getParentid() == 0) {
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

    /**
     * 课程计划创建or修改
     *
     * @param saveTeachplanDto 课程计划创建修改实体
     */
    @Override
    public void teachplanService(SaveTeachplanDto saveTeachplanDto) {

        Long id = saveTeachplanDto.getId();
        Teachplan teachplanData = teachplanMapper.selectById(id);
        //新政
        if (teachplanData == null) {
            Teachplan teachplan = new Teachplan();
            BeanUtils.copyProperties(saveTeachplanDto, teachplan);
            //计算新课程计划 orderBy排序(找到同级数量加1)
            int count = getTeachplanCount(saveTeachplanDto.getCourseId(), saveTeachplanDto.getParentid());
            teachplan.setOrderby(count + 1);
            teachplan.setCreateDate(LocalDateTime.now());
            teachplanMapper.insert(teachplan);
        } else {
            //修改课程计划
            BeanUtils.copyProperties(saveTeachplanDto, teachplanData);
            teachplanData.setChangeDate(LocalDateTime.now());
            teachplanMapper.updateById(teachplanData);
            {{{}}}
        }
    }

    /**
     * 删除课程计划
     *
     * @param teachPlanId 课程计划id
     */
    @Override
    @Transactional
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
     * 移动课程计划
     *
     * @param moveType    移动类型
     * @param teachPlanId 课程计划id
     */
    @Override
    @Transactional
    public void moveTeachPlan(String moveType, Long teachPlanId) {
        Teachplan teachplan = teachplanMapper.selectById(teachPlanId);
        if (teachplan == null) {
            return;
        }
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getParentid, teachplan.getParentid());
        queryWrapper.eq(Teachplan::getCourseId, teachplan.getCourseId());
        //查询到所有同级节点
        List<Teachplan> teachplans = teachplanMapper.selectList(queryWrapper);
        teachplans.sort(Comparator.comparingInt(Teachplan::getOrderby));
        //获取当前移动对象下标
        int i = teachplans.indexOf(teachplan);

        //如果同级别只有一个课程计划，什么也不处理
        if (teachplans.size() <= 1) {
            return;
        }

        Teachplan exchange = null;

        //下移
        if (moveType.equals("movedown")) {
            //下移最后一位不处理
            if (i == (teachplans.size() - 1)) {
                return;
            }
            exchange = teachplans.get(i + 1);
        }

        //上移
        if (moveType.equals("moveup")) {
            //上移第一位不处理
            if (i == 0) {
                return;
            }
            exchange = teachplans.get(i - 1);
        }
        if (exchange == null) {
            XueChengPlusException.cast("移动参数错误");
        }

        //交换排序
        Integer orderby1 = teachplan.getOrderby();
        Integer orderby2 = exchange.getOrderby();
        teachplan.setOrderby(orderby2);
        exchange.setOrderby(orderby1);
        teachplanMapper.updateById(teachplan);
        teachplanMapper.updateById(exchange);
        log.debug("课程计划交换位置，原:{},移:{}", teachplan.getId(), exchange.getId());
    }

    /**
     * 查询同级课程计划的数量
     *
     * @param courseId 课程id
     * @param parentid 父类id
     * @return
     */
    public int getTeachplanCount(Long courseId, Long parentid) {
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId, courseId);
        queryWrapper.eq(Teachplan::getParentid, parentid);
        return teachplanMapper.selectCount(queryWrapper);
    }


    /**
     * 删除课程计划时候更新排序字段
     *
     * @param parentid 父类id
     * @param orderBy  要删除的 排序字段
     */
    public void updateTeachPlanOrderBy(Long parentid, Integer orderBy) {
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getParentid, parentid);
        queryWrapper.gt(Teachplan::getOrderby, orderBy);
        List<Teachplan> teachplans = teachplanMapper.selectList(queryWrapper);
        if (teachplans != null && teachplans.size() > 0) {
            for (Teachplan teachplan : teachplans) {
                Integer orderby = teachplan.getOrderby();
                teachplan.setOrderby(orderby - 1);
                teachplanMapper.updateById(teachplan);
            }
        }
    }

}
