package com.xuecheng.content.service.impl;

import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * packageName com.xuecheng.content.service.impl
 *
 * @author Q
 * @version JDK 8
 * @className CourseCategoryServiceImpl
 * @date 2024/3/22 16:12
 * @description 课程分类接口
 */
@Slf4j
@Service
public class CourseCategoryServiceImpl implements CourseCategoryService {

    @Autowired
    private CourseCategoryMapper courseCategoryMapper;


    /***
     * @description 课程分类查询
     * @param id 根节点id
     * @return List<CourseCategoryTreeDto> 返回根节点所有子节点
     * @author Q
     * @date 2024/3/22 16:11
     */
    @Override
    public List<CourseCategoryTreeDto> queryTreeNodes(String id) {
        //获取根节点的所有下属节点
        List<CourseCategoryTreeDto> list = courseCategoryMapper.selectTreeNodes(id);
        //封装好的数据
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = new ArrayList<>();
        //存放所有节点，方便为其添加子节点
        Map<String, CourseCategoryTreeDto> nodeMap = new HashMap<>();
        //遍历所有数据
        list.stream().forEach(item -> {
            nodeMap.put(item.getId(), item);
            //将根节点的下属节点直接放入
            if (item.getParentid().equals(id)) {
                courseCategoryTreeDtos.add(item);
            }
            //找到子节点，放入上面暂存的父节点中
            //父节点
            String parentid = item.getParentid();
            //父节点对象
            CourseCategoryTreeDto courseCategoryTreeDto = nodeMap.get(parentid);
            if (courseCategoryTreeDto != null) {
                //放入子节点
                List<CourseCategoryTreeDto> childrenTreeNodes = courseCategoryTreeDto.getChildrenTreeNodes();
                if (childrenTreeNodes == null) {
                    courseCategoryTreeDto.setChildrenTreeNodes(new ArrayList<>());
                }
                courseCategoryTreeDto.getChildrenTreeNodes().add(item);
            }
        });
        return courseCategoryTreeDtos;
    }

}
