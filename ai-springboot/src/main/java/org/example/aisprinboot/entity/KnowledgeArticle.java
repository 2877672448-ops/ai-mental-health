package org.example.aisprinboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识文章实体类
 */
@Data
@TableName("knowledge_article")
public class KnowledgeArticle {
    /**
     * 文章ID(UUID)
     */
    @TableId(type = IdType.INPUT)
    private String id;

    /**
     * 分类ID
     */
    @TableField("category_id")
    @JsonProperty("category_id")
    private Long categoryId;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 文章摘要
     */
    private String summary;

    /**
     * 文章内容
     */
    private String content;

    /**
     * 封面图片
     */
    @TableField("cover_image")
    @JsonProperty("cover_image")
    private String coverImage;

    /**
     * 标签
     */
    private String tags;

    /**
     * 作者ID
     */
    @TableField("author_id")
    @JsonProperty("author_id")
    private Long authorId;

    /**
     * 阅读次数
     */
    @TableField("read_count")
    @JsonProperty("read_count")
    private Integer readCount;

    /**
     * 状态 1:已发布
     */
    private Integer status;

    /**
     * 发布时间
     */
    @TableField("published_at")
    @JsonProperty("published_at")
    private LocalDateTime publishedAt;

    /**
     * 创建时间
     */
    @TableField("created_at")
    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    /**
     * 非数据库字段：分类名称（用于关联查询）
     */
    @TableField(exist = false)
    @JsonProperty("category_name")
    private String categoryName;

    /**
     * 非数据库字段：作者名称（用于关联查询）
     */
    @TableField(exist = false)
    @JsonProperty("author_name")
    private String authorName;
}
