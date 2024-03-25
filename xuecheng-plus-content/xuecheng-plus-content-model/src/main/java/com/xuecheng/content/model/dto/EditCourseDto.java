package com.xuecheng.content.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * packageName com.xuecheng.content.model.dto
 *
 * @author Q
 * @version JDK 8
 * @className EditCourseDto
 * @date 2024/3/25 16:00
 * @description 添加课程dto
 */

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "EditCourseDto", description = "修改课程基本信息")
public class EditCourseDto extends AddCourseDto {

    @NotNull(message = "课程id不能为空")
    @ApiModelProperty(value = "课程id", required = true)
    private Long id;
}
