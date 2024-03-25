package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.execption.XueChengPlusException;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.model.po.CourseMarket;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 课程营销信息 服务实现类
 * </p>
 *
 * @author itcast
 */
@Slf4j
@Service
public class CourseMarketServiceImpl extends ServiceImpl<CourseMarketMapper, CourseMarket> {


    /**
     * 修改or新增营销信息
     * @param courseMarket 要修改的营销表信息
     * @return 成功or失败
     */
     public int saveCourseMarket(CourseMarket courseMarket){
         //课程收费，价格校验
         String charge = courseMarket.getCharge();
         if (StringUtils.isBlank(charge)){
             XueChengPlusException.cast("收费规则没有选择");
         }

         //收费
         if (charge.equals("201001")) {
             if (courseMarket.getPrice() == null || courseMarket.getPrice() <= 0) {
                 XueChengPlusException.cast("课程收费价格为空且小于等于0");
             }
         }

         boolean b = saveOrUpdate(courseMarket);
         return b?1:0;
     }

}
