package org.example.aisprinboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 情绪日记实体类
 */
@Data
@TableName("emotion_diary")
public class EmotionDiary {
    /**
     * 日记ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 日记日期
     */
    @TableField("diary_date")
    private LocalDate diaryDate;

    /**
     * 情绪评分(1-10)
     */
    @TableField("mood_score")
    @JsonProperty("mood_score")
    private Integer moodScore;

    /**
     * 主要情绪
     */
    @TableField("dominant_emotion")
    @JsonProperty("dominant_emotion")
    private String dominantEmotion;

    /**
     * 情绪触发因素
     */
    @TableField("emotion_triggers")
    @JsonProperty("emotion_triggers")
    private String emotionTriggers;

    /**
     * 日记内容
     */
    @TableField("diary_content")
    @JsonProperty("diary_content")
    private String diaryContent;

    /**
     * 睡眠质量(1-5)
     */
    @TableField("sleep_quality")
    @JsonProperty("sleep_quality")
    private Integer sleepQuality;

    /**
     * 压力水平(1-5)
     */
    @TableField("stress_level")
    @JsonProperty("stress_level")
    private Integer stressLevel;

    /**
     * AI情绪分析结果(JSON格式)
     */
    @TableField("ai_emotion_analysis")
    private String aiEmotionAnalysis;

    /**
     * AI分析更新时间
     */
    @TableField("ai_analysis_updated_at")
    private LocalDateTime aiAnalysisUpdatedAt;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    /**
     * 非数据库字段：用户昵称（用于关联查询）
     */
    @TableField(exist = false)
    private String nickname;
}
