package com.xuecheng.media.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @description 媒资文件管理业务类
 * @author Mr.M
 * @date 2022/9/10 8:55
 * @version 1.0
 */
public interface MediaFileService {

 /**
  * @description 媒资文件查询方法
  * @param pageParams 分页参数
  * @param queryMediaParamsDto 查询条件
  * @return com.xuecheng.base.model.PageResult<com.xuecheng.media.model.po.MediaFiles>
  * @author Mr.M
  * @date 2022/9/10 8:57
 */
 public PageResult<MediaFiles> queryMediaFiels(Long companyId,PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);

 /***
  * @description 通用文件上传接口
  * @param companyId 机构id
  * @param uploadFileParamsDto 文件信息
  * @param bytes 文件字节数组
  * @param folder 桶下面的目录
  * @param objectName 对象名
  * @return UploadFileResultDto
  * @author Q
  * @date 2024/3/26 17:48
 */
 public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, byte[] bytes, String folder, String objectName);

 /**
  * 保存媒资信息
  *
  * @param companyId           机构id
  * @param fileId              文件id
  * @param uploadFileParamsDto 文件信息
  * @param bucket              桶
  * @param objectName          文件路径
  * @return MediaFiles
  */
 public MediaFiles saveMediaDb(Long companyId, String fileId, UploadFileParamsDto uploadFileParamsDto, String bucket, String objectName);


}
