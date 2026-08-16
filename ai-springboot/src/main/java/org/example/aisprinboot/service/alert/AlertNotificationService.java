package org.example.aisprinboot.service.alert;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.example.aisprinboot.entity.AlertNotification;
import org.example.aisprinboot.entity.AlertRecord;
import org.example.aisprinboot.entity.User;
import org.example.aisprinboot.enumClass.UserStatus;
import org.example.aisprinboot.enumClass.UserType;
import org.example.aisprinboot.mapper.AlertNotificationMapper;
import org.example.aisprinboot.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 危机预警通知服务
 * <p>
 * 双层降级设计：
 * 1. alert.mail.enabled=false 时跳过邮件，只发站内通知
 * 2. 即使 enabled=true，发邮件失败也只记日志，不影响站内通知和预警记录
 * <p>
 * 异步设计：
 * 1. 邮件发送用 @Async("alertMailExecutor")，使用自定义线程池
 * 2. 站内通知同步执行（必做），邮件异步执行（尽力而为）
 *
 * @author PANJU
 */
@Slf4j
@Service
public class AlertNotificationService {

    @Autowired
    private AlertNotificationMapper notificationMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 邮件发送器（如果未配 SMTP，仍可正常启动，发邮件时降级跳过）
     */
    @Autowired(required = false)
    private JavaMailSender mailSender;

    /**
     * 是否启用邮件通知（默认关闭）
     */
    @Value("${alert.mail.enabled:false}")
    private boolean mailEnabled;

    /**
     * 邮件接收人
     */
    @Value("${alert.mail.to:admin@example.com}")
    private String mailTo;

    /**
     * 邮件发件人
     */
    @Value("${alert.mail.from:noreply@example.com}")
    private String mailFrom;

    /**
     * 为一条预警创建站内通知（同步，必做）
     * 给所有启用状态的管理员插入一条 alert_notification
     *
     * @param alertId 预警ID
     * @param record  预警记录（用于邮件内容）
     */
    public void createNotifications(Long alertId, AlertRecord record) {
        // 查询所有启用状态的管理员
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUserType, UserType.ADMIN.getCode())
                .eq(User::getStatus, UserStatus.NORMAL.getCode());
        List<User> admins = userMapper.selectList(wrapper);

        if (admins.isEmpty()) {
            log.warn("未找到任何启用的管理员，预警ID={} 无法创建通知", alertId);
            return;
        }

        // 批量插入通知记录
        for (User admin : admins) {
            AlertNotification notification = new AlertNotification();
            notification.setAlertId(alertId);
            notification.setAdminId(admin.getId());
            notification.setIsRead(0);
            notification.setCreatedAt(LocalDateTime.now());
            notificationMapper.insert(notification);
        }
        log.info("预警通知已创建，alertId={}, 管理员数={}", alertId, admins.size());

        // 邮件异步发送（降级容错）
        if (mailEnabled) {
            sendMailAsync(record);
        }
    }

    /**
     * 异步发送邮件通知（使用自定义线程池 alertMailExecutor）
     * 失败仅记日志，不影响主流程
     *
     * @param record 预警记录
     */
    @Async("alertMailExecutor")
    public void sendMailAsync(AlertRecord record) {
        if (mailSender == null) {
            log.warn("JavaMailSender 未注入，邮件发送跳过");
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(mailTo);
            message.setSubject("【危机预警】新的高危预警需要处理 - 预警ID#" + record.getId());
            message.setText(buildMailContent(record));
            mailSender.send(message);
            log.info("预警邮件已发送，alertId={}", record.getId());
        } catch (Exception e) {
            // 邮件失败仅记日志，不抛异常（不影响站内通知）
            log.warn("预警邮件发送失败 alertId={}, error={}", record.getId(), e.getMessage());
        }
    }

    /**
     * 构建邮件正文
     */
    private String buildMailContent(AlertRecord record) {
        StringBuilder sb = new StringBuilder();
        sb.append("【危机预警通知】\n\n");
        sb.append("预警ID：").append(record.getId()).append("\n");
        sb.append("用户ID：").append(record.getUserId()).append("\n");
        sb.append("预警级别：").append(record.getAlertLevel()).append("（1低危 2中危 3高危）\n");
        sb.append("触发原因：").append(record.getTriggerReason()).append("\n");
        sb.append("创建时间：").append(record.getCreatedAt()).append("\n\n");
        sb.append("AI 分析结果：\n").append(record.getAiAnalysis()).append("\n\n");
        sb.append("请尽快登录后台处理。");
        return sb.toString();
    }

    /**
     * 标记通知为已读
     *
     * @param notificationId 通知ID
     * @param adminId 当前管理员ID（防止越权）
     */
    public void markAsRead(Long notificationId, Long adminId) {
        AlertNotification notification = notificationMapper.selectById(notificationId);
        if (notification == null) {
            return;
        }
        if (!notification.getAdminId().equals(adminId)) {
            log.warn("管理员{}尝试标记非自己的通知{}，已拦截", adminId, notificationId);
            return;
        }
        notification.setIsRead(1);
        notification.setReadAt(LocalDateTime.now());
        notificationMapper.updateById(notification);
    }

    /**
     * 获取管理员未读通知数
     *
     * @param adminId 管理员ID
     * @return 未读数量
     */
    public Long countUnread(Long adminId) {
        LambdaQueryWrapper<AlertNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AlertNotification::getAdminId, adminId)
                .eq(AlertNotification::getIsRead, 0);
        return notificationMapper.selectCount(wrapper);
    }
}
