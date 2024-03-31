package com.xuecheng.media.service;

import com.xuecheng.media.model.po.MediaProcess;

import java.util.List;

/**
 * packageName com.xuecheng.media.service
 *
 * @author Q
 * @version JDK 8
 * @className MediaFileProcessService
 * @date 2024/3/28 14:12
 * @description 媒资文件处理业务方法
 */
public interface MediaFileProcessService {

    /***
     * @description 获取待处理任务
     * @param shardIndex 执行器序号
     * @param shardTotal 分片总数
     * @param count 获取多少条数据
     * @return List<MediaProcess>
     * @author Q
     * @date 2024/3/28 14:13
     */
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count);


    /***
     * @description 处理任务完成信息
     * @param taskId 任务id
     * @param status 任务状态
     * @param fileId 文件id
     * @param url 文件url
     * @param errorMsg 异常信息
     * @author Q
     * @date 2024/3/28 14:23
    */
    public void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg);


}
