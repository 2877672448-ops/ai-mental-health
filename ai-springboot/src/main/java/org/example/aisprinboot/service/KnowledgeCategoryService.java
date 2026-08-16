package org.example.aisprinboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.aisprinboot.entity.KnowledgeCategory;
import org.example.aisprinboot.mapper.KnowledgeCategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 知识分类服务
 */
@Service
public class KnowledgeCategoryService {

    @Autowired
    private KnowledgeCategoryMapper categoryMapper;

    /**
     * 获取分类树结构
     */
    public List<Map<String, Object>> listTree() {
        // 查询所有启用的分类
        LambdaQueryWrapper<KnowledgeCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(KnowledgeCategory::getStatus, 1)
                .orderByAsc(KnowledgeCategory::getSortOrder);
        List<KnowledgeCategory> categories = categoryMapper.selectList(queryWrapper);

        // 组装树形结构
        List<Map<String, Object>> tree = new ArrayList<>();
        Map<Long, List<Map<String, Object>>> parentMap = new java.util.HashMap<>();

        for (KnowledgeCategory category : categories) {
            Map<String, Object> node = new java.util.HashMap<>();
            node.put("id", category.getId());
            node.put("categoryName", category.getCategoryName());
            node.put("parentId", category.getParentId());
            node.put("sortOrder", category.getSortOrder());
            node.put("description", category.getDescription());
            node.put("categoryCode", category.getCategoryCode());
            node.put("children", new ArrayList<>());

            Long parentId = category.getParentId();
            if (parentId == null || parentId == 0) {
                tree.add(node);
            } else {
                parentMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(node);
            }
        }

        // 递归设置子节点
        for (Map<String, Object> node : tree) {
            setChildren(node, parentMap);
        }

        return tree;
    }

    private void setChildren(Map<String, Object> node, Map<Long, List<Map<String, Object>>> parentMap) {
        Long id = (Long) node.get("id");
        List<Map<String, Object>> children = parentMap.get(id);
        if (children != null && !children.isEmpty()) {
            node.put("children", children);
            for (Map<String, Object> child : children) {
                setChildren(child, parentMap);
            }
        }
    }
}
