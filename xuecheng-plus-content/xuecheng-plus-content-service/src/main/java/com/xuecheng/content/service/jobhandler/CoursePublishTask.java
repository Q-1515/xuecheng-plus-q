package com.xuecheng.content.service.jobhandler;

import com.xuecheng.base.execption.XueChengPlusException;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * packageName com.xuecheng.content.service.jobhandler
 *
 * @author Q
 * @version JDK 8
 * @className CoursePublishTask
 * @date 2024/4/2 15:40
 * @description 内容管理处理定时任务
 */
@Slf4j
@Component
public class CoursePublishTask extends MessageProcessAbstract {

    //课程发布消息类型
    public static final String MESSAGE_TYPE = "course_publish";

    @Autowired
    private CoursePublishService coursePublishService;

    //任务调度入口
    @XxlJob("CoursePublishJobHandler")
    public void coursePublishJobHandler() throws Exception {
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        log.debug("shardIndex=" + shardIndex + ",shardTotal=" + shardTotal);
        //参数:分片序号、分片总数、消息类型、一次最多取到的任务数量、一次任务调度执行的超时时间
        process(shardIndex, shardTotal, MESSAGE_TYPE, 5, 60);
    }


    /***
     * @description 课程发布任务处理
     * @param mqMessage 任务信息
     * @return boolean 是否成功
     * @author Q
     * @date 2024/4/2 15:44
     */
    @Override
    public boolean execute(MqMessage mqMessage) {
        //获取消息相关的业务信息
        String businessKey1 = mqMessage.getBusinessKey1();
        long courseId = Integer.parseInt(businessKey1);
        //课程静态化
        generateCourseHtml(mqMessage, courseId);
        //课程缓存
//        saveCourseCache(mqMessage, courseId);
        //课程索引
        saveCourseIndex(mqMessage, courseId);
        return true;
    }

    /***
     * @description 课程页面静态化
     * @param mqMessage 任务信息
     * @param courseId 课程id
     * @author Q
     * @date 2024/4/2 16:07
     */
    public void generateCourseHtml(MqMessage mqMessage, long courseId) {
        log.debug("开始进行课程静态化,课程id:{}", courseId);
        //消息id
        Long id = mqMessage.getId();
        //消息处理的service
        MqMessageService mqMessageService = this.getMqMessageService();
        //消息幂等性处理
        int stageOne = mqMessageService.getStageOne(id);
        if (stageOne == 1) {
            log.debug("课程静态化已处理直接返回，课程id:{}", courseId);
            return;
        }
        //生成静态化页面
        File file = coursePublishService.generateCourseHtml(courseId);
        if (file == null) {
            XueChengPlusException.cast("课程静态化异常");
        }
        //上传静态化页面
        coursePublishService.uploadCourseHtml(courseId, file);
        //保存第一阶段状态
        mqMessageService.completedStageOne(id);
    }

    /***
     * @description 将课程信息缓存至redis
     * @param mqMessage 任务信息
     * @param courseId 课程id

     * @author Q
     * @date 2024/4/2 16:11
     */
    public void saveCourseCache(MqMessage mqMessage, long courseId) {
        log.debug("将课程信息缓存至redis,课程id:{}", courseId);
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    /***
     * @description 保存课程索引信息
     * @param mqMessage 任务信息
     * @param courseId 课程id
     * @author Q
     * @date 2024/4/2 16:10
     */
    public void saveCourseIndex(MqMessage mqMessage, long courseId) {
        log.debug("保存课程索引信息,课程id:{}", courseId);
        //消息id
        Long id = mqMessage.getId();
        //消息处理的service
        MqMessageService mqMessageService = this.getMqMessageService();
        //消息幂等性处理
        int stageThree = mqMessageService.getStageThree(id);
        if (stageThree == 1) {
            log.debug("保存课程索引信息已处理直接返回，课程id:{}", courseId);
            return;
        }
        //远程调用添加es索引
        Boolean b = coursePublishService.saveCourseIndex(courseId);
        if(!b){
            log.error("添加索引失败");
            XueChengPlusException.cast("添加索引失败");
        }
        //保存第三阶段状态
        mqMessageService.completedStageThree(id);
    }


}
