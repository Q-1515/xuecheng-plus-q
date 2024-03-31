package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.service.TeachplanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * packageName com.xuecheng.content.api
 *
 * @author Q
 * @version JDK 8
 * @className TeachplanController
 * @date 2024/3/25 17:30
 * @description 课程计划编辑接口
 */
@Api(value = "课程计划编辑接口", tags = "课程计划编辑接口")
@RestController
public class TeachplanController {

    @Autowired
    private TeachplanService teachplanService;

    @ApiOperation("查询课程计划树形结构")
    @ApiImplicitParam(value = "courseId", name = "课程Id", required = true, dataType = "Long", paramType = "path")
    @GetMapping("/teachplan/{courseId}/tree-nodes")
    public List<TeachplanDto> getTreeNodes(@PathVariable Long courseId) {
        return teachplanService.findTeachplayTree(courseId);
    }


    @ApiOperation("课程计划创建或修改")
    @PostMapping("/teachplan")
    public void saveTeachplan(@RequestBody SaveTeachplanDto teachplan) {
        teachplanService.teachplanService(teachplan);
    }


    @ApiOperation(value = "删除课程计划")
    @DeleteMapping("/teachplan/{teachPlanId}")
    public void removeTeachPlan(@PathVariable Long teachPlanId) {
        teachplanService.removeTeachPlan(teachPlanId);
    }

    @ApiOperation("移动课程计划")
    @PostMapping("/teachplan/{moveType}/{teachPlanId}")
    public void moveTeachPlan(@PathVariable String moveType, @PathVariable Long teachPlanId) {
        teachplanService.moveTeachPlan(moveType, teachPlanId);
    }

    @ApiOperation(value = "课程计划和媒资信息绑定")
    @PostMapping("/teachplan/association/media")
    public void associationMedia(@RequestBody @Validated BindTeachplanMediaDto bindTeachplanMediaDto) {
        teachplanService.associationMedia(bindTeachplanMediaDto);
    }

    @ApiOperation(value = "删除课程计划和媒资信息绑定")
    @DeleteMapping("/teachplan/association/media/{teachPlanId}/{mediaId}")
    public void deleteAssociationMedia(@PathVariable Long teachPlanId,@PathVariable String mediaId) {
        teachplanService.deleteAssociationMedia(teachPlanId,mediaId);
    }
}
