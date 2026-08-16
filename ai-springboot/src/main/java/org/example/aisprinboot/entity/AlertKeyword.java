package org.example.aisprinboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 预警关键词实体类
 *
 * @author PANJU
 */
@Data
@TableName("alert_keyword")
public class AlertKeyword {

    /**
     * 关键词ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关键词内容
     */
    private String keyword;

    /**
     * 分类 SUICIDE/SELF_HARM/VIOLENCE/OTHER
     */
    private String category;

    /**
     * 风险权重
     */
    @TableField("risk_weight")
    private Integer riskWeight;

    /**
     * 状态 1启用 0禁用
     */
    private Integer status;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
