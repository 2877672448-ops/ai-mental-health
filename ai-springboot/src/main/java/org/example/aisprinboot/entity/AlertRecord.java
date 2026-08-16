package org.example.aisprinboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 危机预警记录实体类
 *
 * @author PANJU
 */
@Data
@TableName("alert_record")
public class AlertRecord {

    /**
     * 预警ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 触发的日记ID
     */
    @TableField("diary_id")
    private Long diaryId;

    /**
     * 预警级别 1低 2中 3高
     */
    @TableField("alert_level")
    @JsonProperty("alert_level")
    private Integer alertLevel;

    /**
     * 触发原因（JSON数组，命中的规则列表）
     */
    @TableField("trigger_reason")
    @JsonProperty("trigger_reason")
    private String triggerReason;

    /**
     * AI深度分析结果
     */
    @TableField("ai_analysis")
    @JsonProperty("ai_analysis")
    private String aiAnalysis;

    /**
     * 推荐文章（JSON数组，文章UUID）
     */
    @TableField("recommended_articles")
    @JsonProperty("recommended_articles")
    private String recommendedArticles;

    /**
     * 状态 0未处理 1已处理 2已忽略
     */
    @TableField("status")
    private Integer status;

    /**
     * 处理人ID
     */
    @TableField("handled_by")
    @JsonProperty("handled_by")
    private Long handledBy;

    /**
     * 处理时间
     */
    @TableField("handled_at")
    @JsonProperty("handled_at")
    private LocalDateTime handledAt;

    /**
     * 创建时间
     */
    @TableField("created_at")
    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    /**
     * 非数据库字段：用户昵称（关联查询用）
     */
    @TableField(exist = false)
    private String userNickname;

    /**
     * 非数据库字段：日记内容预览（关联查询用）
     */
    @TableField(exist = false)
    private String diaryContentPreview;
}
