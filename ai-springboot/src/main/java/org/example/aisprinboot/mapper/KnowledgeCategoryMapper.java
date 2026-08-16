package org.example.aisprinboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.aisprinboot.entity.KnowledgeCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 知识分类 Mapper 接口
 */
@Mapper
public interface KnowledgeCategoryMapper extends BaseMapper<KnowledgeCategory> {
}
