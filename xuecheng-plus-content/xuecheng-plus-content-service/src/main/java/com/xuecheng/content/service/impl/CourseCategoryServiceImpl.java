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

/**
 * 课程分类 Service
 *
 * @author QLJ
 * @date 2023-2-20 0020 19:55
 */
@Slf4j
@Service
public class CourseCategoryServiceImpl implements CourseCategoryService {

    @Autowired
    private CourseCategoryMapper courseCategoryMapper;


    /**
     * 查询课程分类
     * @param id 根节点id
     * @return 课程分类
     */
    @Override
    public List<CourseCategoryTreeDto> queryTreeNodes(String id) {
        List<CourseCategoryTreeDto> courseCategoryTree = courseCategoryMapper.selectTreeNodes(id);

        //封装好的对象集合
        List<CourseCategoryTreeDto> courseCategoryTreeDtoList = new ArrayList<>();

        //方便找子节点的父节点，顶一个map
        HashMap<String, CourseCategoryTreeDto> nodeMap = new HashMap<>();
        //将数据封装到List中,只包括了更节电的直接下属节点
        courseCategoryTree.stream().forEach(item->{
            nodeMap.put(item.getId(),item);
            //拿取id的下属节点
            if (item.getParentid().equals(id)){
                courseCategoryTreeDtoList.add(item);
            }

            String parentid = item.getParentid();
            //找到该节点的父对象
            CourseCategoryTreeDto parentNode = nodeMap.get(parentid);
            if (parentNode != null){
                List<CourseCategoryTreeDto> childrenTreeNodes = parentNode.getChildrenTreeNodes();
                if (childrenTreeNodes == null){
                    parentNode.setChildrenTreeNodes(new ArrayList<>());
                }
                //找到子节点放入父节点的集合
                parentNode.getChildrenTreeNodes().add(item);
            }
        });

        return courseCategoryTreeDtoList;
    }
}
