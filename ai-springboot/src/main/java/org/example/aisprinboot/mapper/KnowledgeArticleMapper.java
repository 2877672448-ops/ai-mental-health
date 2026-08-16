package org.example.aisprinboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.aisprinboot.entity.KnowledgeArticle;
import org.apache.ibatis.annotations.Mapper;

/**
 * 知识文章 Mapper 接口
 */
@Mapper
public interface KnowledgeArticleMapper extends BaseMapper<KnowledgeArticle> {
}
