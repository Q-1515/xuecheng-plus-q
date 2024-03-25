package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import lombok.Data;

import java.util.List;

/**
 * packageName com.xuecheng.content.model.dto
 *
 * @author Q
 * @version JDK 8
 * @className TeachplanDto
 * @date 2024/3/25 17:27
 * @description 课程计划树型结构dto
 */
@Data
public class TeachplanDto extends Teachplan {
    //课程计划关联的媒资信息
    TeachplanMedia teachplanMedia;

    //子结点
    List<TeachplanDto> teachPlanTreeNodes;
}
