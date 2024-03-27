package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.execption.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.minio.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description 媒体文件服务
 * @date 2022/9/10 8:58
 */
@Slf4j
@Service
public class MediaFileServiceImpl implements MediaFileService {

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MediaFileService mediaFileService;

    @Value("${minio.bucket.files}")
    private String bucket_files;

    @Value("${minio.bucket.videofiles}")
    private String videofiles;

    @Override
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();
        String filename = queryMediaParamsDto.getFilename();
        String fileType = queryMediaParamsDto.getFileType();
        queryWrapper.like(StringUtils.isNotEmpty(filename), MediaFiles::getFilename, filename);
        queryWrapper.eq(StringUtils.isNotEmpty(fileType), MediaFiles::getFileType, fileType);
        queryWrapper.eq(MediaFiles::getCompanyId, companyId);
        queryWrapper.orderByDesc(MediaFiles::getChangeDate);

        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return mediaListResult;

    }

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
    @Override
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, byte[] bytes, String folder, String objectName) {

        //得到文件的md5值
        String fileMd5 = DigestUtils.md5Hex(bytes);

        //目录不存在生成路径
        if (StringUtils.isEmpty(folder)) {
            //自动生成目录的路径 按年月日生成，
            folder = getFileFolder(new Date(), true, true, true);
        } else if (folder.indexOf("/") < 0) {
            folder = folder + "/";
        }

        //文件名称
        String filename = uploadFileParamsDto.getFilename();
        if (StringUtils.isEmpty(objectName)) {
            //如果objectName为空，使用文件的md5值为objectName
            objectName = fileMd5 + filename.substring(filename.lastIndexOf("."));
        }
        //上传到minio
        objectName = folder + objectName;
        uploadMinio(bytes, bucket_files, objectName, uploadFileParamsDto.getContentType());

        //数据库保存文件信息
        MediaFiles mediaFiles = mediaFileService.saveMediaDb(companyId, fileMd5, uploadFileParamsDto, bucket_files, objectName);
        //准备返回数据
        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
        BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);
        return uploadFileResultDto;
    }


    /**
     * 上传文件到minio
     *
     * @param bytes      文件数组
     * @param bucket     桶
     * @param objectName 上传的文件路径
     */
    public void uploadMinio(byte[] bytes, String bucket, String objectName, String Type) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

        if (StringUtils.isEmpty(Type)) {
            Type = getContentType(objectName);
        }
        PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                .bucket(bucket)
                .object(objectName)
                //InputStream stream, long objectSize 对象大小, long partSize 分片大小(-1表示5M,最大不要超过5T，最多10000)
                .stream(byteArrayInputStream, byteArrayInputStream.available(), -1)
                .contentType(Type)
                .build();
        //上传到minio
        try {
            minioClient.putObject(putObjectArgs);
        } catch (Exception e) {
            log.error("上传到Minio失败：{}", e.getMessage());
            XueChengPlusException.cast("上传到Minio失败");
        }
    }

    /**
     * 上传本地文件到minio
     *
     * @param FilePath   本地路径
     * @param bucket     桶
     * @param objectName 上传的文件路径
     */
    public void uploadMinio(String FilePath, String bucket, String objectName) {
        String contentType = getContentType(objectName);
        try {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)//同一个桶内对象名不能重复
                    .filename(FilePath)
                    .contentType(contentType)
                    .build();
            //上传
            minioClient.uploadObject(uploadObjectArgs);
            log.info("上传成功:{}", FilePath);
        } catch (Exception e) {
            XueChengPlusException.cast("本地文件上传到Miniow失败");
        }
    }

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
    @Transactional
    public MediaFiles saveMediaDb(Long companyId, String fileId, UploadFileParamsDto uploadFileParamsDto, String bucket, String objectName) {
        //保存到数据库
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
        if (mediaFiles == null) {
            mediaFiles = new MediaFiles();

            //封装数据
            BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
            mediaFiles.setId(fileId);
            mediaFiles.setFileId(fileId);
            mediaFiles.setCompanyId(companyId);
            mediaFiles.setBucket(bucket);
            mediaFiles.setFilePath(objectName);
            //只有图片和MP4视频文件支持url预览
            String contentType = getContentType(uploadFileParamsDto.getFilename());
            if (contentType.contains("image") || contentType.contains("mp4")) {
                mediaFiles.setUrl("/" + bucket + "/" + objectName);
            }
            mediaFiles.setCreateDate(LocalDateTime.now());
            mediaFiles.setStatus("1");
            mediaFiles.setAuditStatus("002003");
            //插入文件表
            mediaFilesMapper.insert(mediaFiles);
        }
        return mediaFiles;
    }

    /***
     * @description 检查文件是否存在
     * @param fileMd5 文件的md5
     * @return RestResponse<Boolean>
     * @author Q
     * @date 2024/3/27 18:12
     */
    @Override
    public RestResponse<Boolean> checkFile(String fileMd5) {
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        //查看数据库书否存在
        if (mediaFiles == null) {
            return RestResponse.success(false);
        }
        //Minio中不存在
        Boolean b = fileIsExistence(mediaFiles.getBucket(), mediaFiles.getFilePath());
        return RestResponse.validfail(b, b ? "文件存在" : "文件不存在");
    }

    /***
     * @description 检查分块是否存在
     * @param fileMd5 文件的md5
     * @param chunkIndex 分块序号
     * @return RestResponse<Boolean>
     * @author Q
     * @date 2024/3/27 18:13
     */
    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        String filePath = chunkFileFolderPath + chunkIndex;
        Boolean b = fileIsExistence(videofiles, filePath);
        return RestResponse.success(b, b ? "文件存在" : "文件不存在");
    }


    /***
     * @description 上传分块
     * @param fileMd5 文件的md5
     * @param chunk 分块序号
     * @param bytes 文件字节
     * @return RestResponse
     * @author Q
     * @date 2024/3/27 18:51
     */
    @Override
    public RestResponse<Boolean> uploadChunk(String fileMd5, int chunk, byte[] bytes) {
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        String filePath = chunkFileFolderPath + chunk;
        try {
            uploadMinio(bytes, videofiles, filePath, null);
        } catch (Exception e) {
            log.error("上传分块文件失败:{}", e.getMessage());
            return RestResponse.validfail(false, "上传分块文件失败");
        }
        return RestResponse.success(true, "上传分块文件成功");
    }

    /***
     * @description 合并分块
     * @param companyId  机构id
     * @param fileMd5 文件md5
     * @param chunkTotal 分块总和
     * @param uploadFileParamsDto 文件信息
     * @return RestResponse
     * @author Q
     * @date 2024/3/27 19:28
     */
    @Override
    public RestResponse mergechunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {
        File[] files = null;
        File merge = null;
        try {
            //下载分块
            files = checkChunkStatus(fileMd5, chunkTotal);
            //获取扩展名
            String filename = uploadFileParamsDto.getFilename();
            String extension = filename.substring(filename.lastIndexOf("."));

            //创建临时合并后文件
            try {
                merge = File.createTempFile("merge", extension);
            } catch (IOException e) {
                log.error("创建零时合并文件失败:{}", e.getMessage());
                XueChengPlusException.cast("创建零时合并文件失败");
            }
            //合并文件
            try (RandomAccessFile r_write = new RandomAccessFile(merge, "rw")) {
                byte[] b = new byte[1024];
                for (File file : files) {
                    try (RandomAccessFile r_read = new RandomAccessFile(file, "r")) {
                        int len = -1;
                        while ((len = r_read.read(b)) != -1) {
                            r_write.write(b, 0, len);
                        }
                    }
                }
            } catch (IOException e) {
                log.error("合并文件失败:{}", e.getMessage());
                XueChengPlusException.cast("合并文件失败");
            }

            //校验合并文件
            try {
                FileInputStream fileInputStream = new FileInputStream(merge);
                String mergeMd5 = DigestUtils.md5Hex(fileInputStream);
                if (!mergeMd5.equals(fileMd5)) {
                    XueChengPlusException.cast("合并文件校验不通过");
                }
            } catch (IOException e) {
                log.error(filename + "-合并文件校验:{},原始文件md5:{}", merge.getAbsolutePath(), fileMd5);
                XueChengPlusException.cast("合并文件校验出错");
            }

            //将合并后的文件上传到Minio
            String objName = getFilePathByMd5(fileMd5, extension);
            uploadMinio(merge.getAbsolutePath(), videofiles, objName);

            //媒资信息存贮
            uploadFileParamsDto.setFileSize(merge.length()); //文件大小
            mediaFileService.saveMediaDb(companyId, fileMd5, uploadFileParamsDto, videofiles, objName);
            return RestResponse.success(true);
        } finally {
            if (files != null) {
                //删除临时文件
                for (File file : files) {
                    try {
                        Files.delete(file.toPath());
                    } catch (Exception e) {
                        log.error("临时分块文件删除失败:{}", file.getAbsolutePath());
                    }
                }
            }
            if (merge != null) {
                //删除临时合并文件
                try {
                    Files.delete(merge.toPath());
                } catch (Exception e) {
                    log.error("临时合并文件删除失败:{}", merge.getAbsolutePath());
                }
            }

        }
    }

    /***
     * @description Minio判断文件是否存在
     * @param bucket 桶
     * @param objectName 文件路径+名称
     * @return Boolean
     * @author Q
     * @date 2024/3/27 18:42
     */
    private Boolean fileIsExistence(String bucket, String objectName) {
        try {
            GetObjectResponse fileInputStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .build());
            if (fileInputStream == null) {
                return false;
            }
            fileInputStream.close();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 下载所有分块
     *
     * @param fileMd5    文件md5
     * @param chunkTotal 分片总数
     * @return 文件数组
     */
    private File[] checkChunkStatus(String fileMd5, int chunkTotal) {
        //得到分块文件的目录路径
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        File[] files = new File[chunkTotal];
        //检查分块文件是否上传完毕
        for (int i = 0; i < chunkTotal; i++) {
            String chunkFilePath = chunkFileFolderPath + i;
            //下载文件
            File chunkFile = null;
            try {
                chunkFile = File.createTempFile("chunk" + i, null);
            } catch (IOException e) {
                e.printStackTrace();
                XueChengPlusException.cast("下载分块时创建临时文件出错");
            }
            files[i] = downloadFileFromMinIO(chunkFile, videofiles, chunkFilePath);
        }
        return files;
    }

    /**
     * 从minio下载文件
     *
     * @param file       下载文件对象
     * @param bucket     桶
     * @param objectName 对象路径
     * @return 文件对象
     */
    public File downloadFileFromMinIO(File file, String bucket, String objectName) {

        try (InputStream fileInputStream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectName)
                        .build());
             OutputStream fileOutputStream = Files.newOutputStream(file.toPath());
        ) {
            IOUtils.copy(fileInputStream, fileOutputStream);
        } catch (IOException e) {
            XueChengPlusException.cast("下载文件" + objectName + "出错");
        } catch (Exception e) {
            log.error("objectName文件不存在:{}", e.getMessage());
            XueChengPlusException.cast("文件不存在" + objectName);
        }
        return file;
    }


    /**
     * 得到分块文件路径
     *
     * @param fileMd5 文件md5
     * @return 分块文件的目录
     */
    private String getChunkFileFolderPath(String fileMd5) {
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + "chunk" + "/";
    }

    /**
     * 获取合并文件路径
     *
     * @param fileMd5 文件md5
     * @param fileExt 文件后缀
     * @return 合并文件路径
     */
    private String getFilePathByMd5(String fileMd5, String fileExt) {
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + fileMd5 + fileExt;
    }


    /**
     * 获取文件类型 ContentType
     *
     * @param objectName 对象名
     * @return ContentType
     */
    private static String getContentType(String objectName) {
        //默认存入类型二进制流
        String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        //扩展名存在
        if (objectName.indexOf(".") > 0) {
            String extension = objectName.substring(objectName.lastIndexOf("."));
            ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
            //合理扩展名
            if (extensionMatch != null) {
                contentType = extensionMatch.getContentType().getMimeType();
            }
        }
        return contentType;
    }

    /**
     * 根据日期返回路径
     *
     * @param date  日期
     * @param year  年
     * @param month 月
     * @param day   日
     * @return String 日期路径
     */
    private String getFileFolder(Date date, boolean year, boolean month, boolean day) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        //获取当前日期字符串
        String dateString = sdf.format(date);
        //取出年、月、日
        String[] dateStringArray = dateString.split("-");
        StringBuffer folderString = new StringBuffer();
        if (year) {
            folderString.append(dateStringArray[0]);
            folderString.append("/");
        }
        if (month) {
            folderString.append(dateStringArray[1]);
            folderString.append("/");
        }
        if (day) {
            folderString.append(dateStringArray[2]);
            folderString.append("/");
        }
        return folderString.toString();
    }
}
