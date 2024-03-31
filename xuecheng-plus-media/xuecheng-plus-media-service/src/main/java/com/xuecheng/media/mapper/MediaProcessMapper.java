package com.xuecheng.media.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.media.model.po.MediaProcess;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface MediaProcessMapper extends BaseMapper<MediaProcess> {

    @Select("SELECT * FROM `media_process` t WHERE id % #{shardTotal} = #{shardIndex} AND `status` = 1 LIMIT #{cpu}")
    public List<MediaProcess> selectListByShardIndex(@Param("shardTotal") int shardTotal,
                                                     @Param("shardIndex") int shardIndex,
                                                     @Param("cpu") int cpu);

}
