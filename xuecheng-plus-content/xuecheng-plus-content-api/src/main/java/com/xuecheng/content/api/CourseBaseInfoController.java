package com.xuecheng.content.api;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.model.dto.QueryCourseParamsDto;
import com.xuecheng.model.po.CourseBase;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.val;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * packageName com.xuecheng.content.api
 *
 * @author Q
 * @version JDK 8
 * @className CourseBaseInfoController
 * @date 2024/3/21 19:45
 * @description 内容管理模块
 */

@RestController
@Api(value = "内容管理接口",tags = "内容管理接口")
public class CourseBaseInfoController {

    @ApiOperation("课程查询接口")
    @PostMapping("/content/list")
    public PageResult<CourseBase> getall(PageParams pageParams, @RequestBody QueryCourseParamsDto queryCourseParamsDto){
        CourseBase courseBase = new CourseBase();
        courseBase.setCreateDate(LocalDateTime.now());
        List list = new ArrayList<>();
        list.add(courseBase);
        PageResult courseBasePageResult = new PageResult<CourseBase>(list, 10, 1, 10);
        return courseBasePageResult;
    }

}
