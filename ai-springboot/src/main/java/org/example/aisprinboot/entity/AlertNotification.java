package org.example.aisprinboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员预警通知实体类
 *
 * @author PANJU
 */
@Data
@TableName("alert_notification")
public class AlertNotification {

    /**
     * 通知ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联预警ID
     */
    @TableField("alert_id")
    private Long alertId;

    /**
     * 接收管理员ID
     */
    @TableField("admin_id")
    private Long adminId;

    /**
     * 是否已读 0未读 1已读
     */
    @TableField("is_read")
    private Integer isRead;

    /**
     * 阅读时间
     */
    @TableField("read_at")
    private LocalDateTime readAt;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
