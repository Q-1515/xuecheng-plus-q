package com.xuecheng.media.service.im;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessHistoryMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.model.po.MediaProcessHistory;
import com.xuecheng.media.service.MediaFileProcessService;
import com.xuecheng.media.service.MediaFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * packageName com.xuecheng.media.service.im
 *
 * @author Q
 * @version JDK 8
 * @className MediaFileProcessServiceImpl
 * @date 2024/3/28 14:15
 * @description 媒资文件处理业务方法
 */
@Slf4j
@Service
public class MediaFileProcessServiceImpl implements MediaFileProcessService {

    @Autowired
    private MediaProcessMapper mediaProcessMapper;

    @Autowired
    private MediaProcessHistoryMapper mediaProcessHistoryMapper;

    @Autowired
    private MediaFilesMapper mediaFilesMapper;

    /***
     * @description 获取待处理任务
     * @param shardIndex 执行器序号
     * @param shardTotal 分片总数
     * @param count 获取多少条数据
     * @return List<MediaProcess>
     * @author Q
     * @date 2024/3/28 14:13
     */
    @Override
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count) {
        return mediaProcessMapper.selectListByShardIndex(shardTotal, shardIndex, count);
    }

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
    @Transactional
    @Override
    public void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg) {
        MediaProcess mediaProcess = mediaProcessMapper.selectById(taskId);
        if (mediaProcess == null) {
            log.error("更新任务转态时：{}为空",taskId);
            return;
        }
        //任务失败
        if ("3".equals(status)){
            MediaProcess mediaProcessUp = new MediaProcess();
            mediaProcessUp.setStatus(status);
            mediaProcessUp.setErrormsg(errorMsg);
            LambdaQueryWrapper<MediaProcess> wrapper = new LambdaQueryWrapper<MediaProcess>().eq(MediaProcess::getId, taskId);
            //更新状态和异常信息
            mediaProcessMapper.update(mediaProcessUp, wrapper);
            return;
        }

        //任务成功
        if ("2".equals(status)){
            //更新待处理表
            mediaProcess.setStatus(status);
            mediaProcess.setUrl(url);
            mediaProcess.setFinishDate(LocalDateTime.now());
            mediaProcessMapper.updateById(mediaProcess);

            //更新文件表url
            MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
            mediaFiles.setUrl(url);
            mediaFilesMapper.updateById(mediaFiles);
        }
        //任务成功向历史任务表表添加记录
        MediaProcessHistory mediaProcessHistory = new MediaProcessHistory();
        BeanUtils.copyProperties(mediaProcess,mediaProcessHistory);
        mediaProcessHistoryMapper.insert(mediaProcessHistory);

        //删除任务表数据
        mediaProcessMapper.deleteById(taskId);
    }
}
