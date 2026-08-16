package org.example.aisprinboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.aisprinboot.entity.KnowledgeArticle;
import org.example.aisprinboot.entity.KnowledgeCategory;
import org.example.aisprinboot.entity.User;
import org.example.aisprinboot.exception.BusionessException;
import org.example.aisprinboot.mapper.KnowledgeArticleMapper;
import org.example.aisprinboot.mapper.KnowledgeCategoryMapper;
import org.example.aisprinboot.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 知识文章服务
 */
@Service
public class KnowledgeArticleService {

    @Autowired
    private KnowledgeArticleMapper articleMapper;

    @Autowired
    private KnowledgeCategoryMapper categoryMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 分页获取文章列表
     */
    public Page<KnowledgeArticle> getPage(Page<KnowledgeArticle> page, Map<String, Object> params, boolean isAdmin) {
        LambdaQueryWrapper<KnowledgeArticle> queryWrapper = new LambdaQueryWrapper<>();

        // 如果不是管理员，只查询已发布的文章
        if (!isAdmin) {
            queryWrapper.eq(KnowledgeArticle::getStatus, 1);
        }

        // 分类ID过滤
        if (params.containsKey("categoryId")) {
            queryWrapper.eq(KnowledgeArticle::getCategoryId, params.get("categoryId"));
        }

        // 标题模糊搜索
        if (params.containsKey("keyword")) {
            queryWrapper.like(KnowledgeArticle::getTitle, params.get("keyword").toString());
        }

        // 状态过滤
        if (params.containsKey("status")) {
            queryWrapper.eq(KnowledgeArticle::getStatus, params.get("status"));
        }

        // 排序
        String sortField = params.getOrDefault("sortField", "publishedAt").toString();
        String sortDirection = params.getOrDefault("sortDirection", "desc").toString();
        if ("desc".equalsIgnoreCase(sortDirection)) {
            queryWrapper.orderByDesc(KnowledgeArticle::getPublishedAt);
        } else {
            queryWrapper.orderByAsc(KnowledgeArticle::getPublishedAt);
        }

        Page<KnowledgeArticle> result = articleMapper.selectPage(page, queryWrapper);

        // 填充分类名称和作者名称
        for (KnowledgeArticle article : result.getRecords()) {
            fillExtraInfo(article);
        }

        return result;
    }

    /**
     * 获取文章详情
     */
    public KnowledgeArticle getById(String id) {
        KnowledgeArticle article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusionessException("文章不存在");
        }
        // 增加阅读量
        article.setReadCount(article.getReadCount() + 1);
        articleMapper.updateById(article);

        fillExtraInfo(article);
        return article;
    }

    /**
     * 创建文章
     */
    public KnowledgeArticle create(KnowledgeArticle article, Long operatorId) {
        article.setId(UUID.randomUUID().toString());
        article.setAuthorId(operatorId);
        article.setReadCount(0);
        article.setStatus(1); // 默认已发布
        article.setPublishedAt(LocalDateTime.now());
        article.setCreatedAt(LocalDateTime.now());
        article.setUpdatedAt(LocalDateTime.now());
        
        articleMapper.insert(article);
        fillExtraInfo(article);
        return article;
    }

    /**
     * 更新文章
     */
    public KnowledgeArticle update(String id, KnowledgeArticle article) {
        KnowledgeArticle existing = articleMapper.selectById(id);
        if (existing == null) {
            throw new BusionessException("文章不存在");
        }

        article.setId(id);
        article.setUpdatedAt(LocalDateTime.now());
        articleMapper.updateById(article);

        fillExtraInfo(article);
        return article;
    }

    /**
     * 更新文章状态
     */
    public void changeStatus(String id, Integer status) {
        KnowledgeArticle article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusionessException("文章不存在");
        }

        article.setStatus(status);
        article.setUpdatedAt(LocalDateTime.now());
        articleMapper.updateById(article);
    }

    /**
     * 删除文章
     */
    public void delete(String id) {
        KnowledgeArticle article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusionessException("文章不存在");
        }
        articleMapper.deleteById(id);
    }

    /**
     * 填充额外信息（分类名、作者名）
     */
    private void fillExtraInfo(KnowledgeArticle article) {
        if (article.getCategoryId() != null) {
            KnowledgeCategory category = categoryMapper.selectById(article.getCategoryId());
            if (category != null) {
                article.setCategoryName(category.getCategoryName());
            }
        }

        if (article.getAuthorId() != null) {
            User user = userMapper.selectById(article.getAuthorId());
            if (user != null) {
                article.setAuthorName(user.getNickname() != null ? user.getNickname() : user.getUsername());
            }
        }
    }
}
