package com.xuecheng.media.service.jobHandler;

import com.xuecheng.base.execption.XueChengPlusException;
import com.xuecheng.base.utils.Mp4VideoUtil;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileProcessService;
import com.xuecheng.media.service.MediaFileService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * XxlJob开发示例（Bean模式）
 * <p>
 * 开发步骤：
 * 1、任务开发：在Spring Bean实例中，开发Job方法；
 * 2、注解配置：为Job方法添加注解 "@XxlJob(value="自定义jobhandler名称", init = "JobHandler初始化方法", destroy = "JobHandler销毁方法")"，注解value值对应的是调度中心新建任务的JobHandler属性的值。
 * 3、执行日志：需要通过 "XxlJobHelper.log" 打印执行日志；
 * 4、任务结果：默认任务结果为 "成功" 状态，不需要主动设置；如有诉求，比如设置任务结果为失败，可以通过 "XxlJobHelper.handleFail/handleSuccess" 自主设置任务结果；
 *
 * @author xuxueli 2019-12-11 21:52:51
 */
@Slf4j
@Component
public class SampleXxlJob {
    private static Logger logger = LoggerFactory.getLogger(SampleXxlJob.class);

    @Autowired
    private MediaFileProcessService mediaFileProcessService;

    @Autowired
    private MediaFileService mediaFileService;

    @Value("${videoprocess.ffmpegpath}")
    String ffmpegpath;

    /**
     * 1、简单任务示例（Bean模式）
     */
    @XxlJob("demoJobHandler")
    public void demoJobHandler() throws Exception {
        XxlJobHelper.log("XXL-JOB, Hello World.");

        for (int i = 0; i < 5; i++) {
            XxlJobHelper.log("beat at:" + i);
            TimeUnit.SECONDS.sleep(2);
        }
        // default success
    }


    /**
     * 视屏处理分片广播任务
     */
    @XxlJob("videoJobHander")
    public void shardingJobHandler() throws Exception {

        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();

        //查询待处理任务
        List<MediaProcess> mediaProcessList = mediaFileProcessService.getMediaProcessList(shardIndex, shardTotal, 4);
        if (mediaProcessList == null || mediaProcessList.isEmpty()) {
            log.info("查询到处理视频为0");
            return;
        }

        //多线程处理
        int size = mediaProcessList.size();
        ExecutorService threadPool = Executors.newFixedThreadPool(size);
        CountDownLatch countDownLatch = new CountDownLatch(size);
        mediaProcessList.forEach(mediaProcess -> {
            threadPool.execute(() -> {
                //幂等性检测
                String status = mediaProcess.getStatus();
                if ("2".equals(status)) {
                    log.info("视屏已处理：{}", mediaProcess.getFilePath());
                    countDownLatch.countDown();
                    return;
                }

                //临时转码文件
                //下载文件
                File originalFile = null;
                File mp4File = null;
                String bucket = mediaProcess.getBucket();
                String filePath = mediaProcess.getFilePath();
                String fileId = mediaProcess.getFileId();
                try {
                    originalFile = File.createTempFile("original", null);
                    mp4File = File.createTempFile("mp4", ".mp4");
                } catch (IOException e) {
                    log.error("创建零时合并文件失败:{}", e.getMessage());
                    XueChengPlusException.cast("创建零时转码文件失败");
                    countDownLatch.countDown();
                    return;
                }
                //下载原始视频进行转码
                try {
                    originalFile = mediaFileService.downloadFileFromMinIO(originalFile, bucket, filePath);
                } catch (Exception e) {
                    log.error("下载原始视频：{}", e.getMessage());
                    countDownLatch.countDown();
                    return;
                }
                //转码
                String mp4_name = fileId + ".mp4";
                String mp4_path = mp4File.getAbsolutePath();
                Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpegpath, originalFile.getAbsolutePath(), mp4_name, mp4_path);
                String result = videoUtil.generateMp4();
                status = "3"; //默认初始值失败
                String url = null;
                //转换成功
                if ("success".equals(result)) {
                    //上传minio
                    String filePathByMd5 = getFilePathByMd5(fileId, ".mp4");
                    try {
                        mediaFileService.uploadMinio(mp4_path, bucket, filePathByMd5);
                    } catch (Exception e) {
                        log.error("上传文件出错：{}", e.getMessage());
                        countDownLatch.countDown();
                        return;
                    }
                    status = "2";
                    url = "/" + bucket + "/" + filePathByMd5;
                }
                //保存处理结果
                try {
                    mediaFileProcessService.saveProcessFinishStatus(mediaProcess.getId(), status, fileId, url, result);
                } catch (Exception e) {
                    log.error("保存任务处理结果出错：{}", e.getMessage());
                    countDownLatch.countDown();
                    return;
                } finally {
                    if (originalFile != null) {
                        //删除临时转换的文件
                        try {
                            Files.delete(originalFile.toPath());
                        } catch (Exception e) {
                            log.error("删除临时转换文件失败:{}", originalFile.getAbsolutePath());
                        }
                    }
                }
                //计数器减一
                countDownLatch.countDown();
            });
        });
        //阻塞到任务执行完成
        countDownLatch.await(30, TimeUnit.MINUTES);
    }

    /**
     * 获取存储文件路径
     *
     * @param fileMd5 文件md5
     * @param fileExt 文件后缀
     * @return 合并文件路径
     */
    private String getFilePathByMd5(String fileMd5, String fileExt) {
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + fileMd5 + fileExt;
    }


}
