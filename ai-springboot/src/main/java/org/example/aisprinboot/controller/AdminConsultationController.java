package org.example.aisprinboot.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.aisprinboot.common.Result;
import org.example.aisprinboot.entity.ConsultationMessage;
import org.example.aisprinboot.entity.ConsultationSession;
import org.example.aisprinboot.entity.User;
import org.example.aisprinboot.mapper.ConsultationMessageMapper;
import org.example.aisprinboot.mapper.ConsultationSessionMapper;
import org.example.aisprinboot.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 后台咨询记录管理控制器
 */
@RestController
@RequestMapping("/api/psychological-chat")
public class AdminConsultationController {

    @Autowired
    private ConsultationSessionMapper sessionMapper;

    @Autowired
    private ConsultationMessageMapper messageMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 获取会话列表（分页）
     */
    @GetMapping("/sessions")
    public Result<?> getSessions(
            @RequestParam(defaultValue = "1") int currentPage,
            @RequestParam(defaultValue = "10") int size) {

        // 查询会话列表
        LambdaQueryWrapper<ConsultationSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ConsultationSession::getStartedAt);

        Page<ConsultationSession> page = sessionMapper.selectPage(new Page<>(currentPage, size), wrapper);

        // 获取所有会话的用户ID
        List<Long> userIds = page.getRecords().stream()
                .map(ConsultationSession::getUserId)
                .distinct()
                .collect(Collectors.toList());

        // 查询用户信息
        final Map<Long, User> userMap;
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));
        } else {
            userMap = new HashMap<>();
        }

        // 查询每个会话的消息数量和最后一条消息
        List<Map<String, Object>> records = page.getRecords().stream().map(session -> {
            Map<String, Object> record = new HashMap<>();
            record.put("id", session.getId());

            // 用户信息
            User user = userMap.get(session.getUserId());
            if (user != null) {
                record.put("userNickname", user.getNickname());
                record.put("userAvatar", user.getAvatar());
            } else {
                record.put("userNickname", "未知用户");
                record.put("userAvatar", null);
            }

            // 会话标题
            record.put("sessionTitle", session.getSessionTitle() != null ? session.getSessionTitle() : "咨询会话");

            // 查询消息数量
            LambdaQueryWrapper<ConsultationMessage> msgWrapper = new LambdaQueryWrapper<>();
            msgWrapper.eq(ConsultationMessage::getSessionId, session.getId());
            Long messageCount = messageMapper.selectCount(msgWrapper);
            record.put("messageCount", messageCount);

            // 查询最后一条消息
            LambdaQueryWrapper<ConsultationMessage> lastMsgWrapper = new LambdaQueryWrapper<>();
            lastMsgWrapper.eq(ConsultationMessage::getSessionId, session.getId())
                    .orderByDesc(ConsultationMessage::getCreatedAt)
                    .last("LIMIT 1");
            ConsultationMessage lastMessage = messageMapper.selectOne(lastMsgWrapper);
            if (lastMessage != null) {
                String preview = lastMessage.getContent();
                if (preview != null && preview.length() > 50) {
                    preview = preview.substring(0, 50) + "...";
                }
                record.put("lastMessageContent", preview);
                record.put("lastMessageTime", lastMessage.getCreatedAt());
            } else {
                record.put("lastMessageContent", "暂无消息");
                record.put("lastMessageTime", session.getStartedAt());
            }

            record.put("startedAt", session.getStartedAt());

            return record;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", page.getTotal());
        result.put("current", page.getCurrent());
        result.put("size", page.getSize());

        return Result.ok(result);
    }

    /**
     * 获取会话的消息详情
     */
    @GetMapping("/sessions/{id}/messages")
    public Result<?> getSessionMessages(@PathVariable Long id) {
        // 查询消息列表
        LambdaQueryWrapper<ConsultationMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConsultationMessage::getSessionId, id)
                .orderByAsc(ConsultationMessage::getCreatedAt);

        List<ConsultationMessage> messages = messageMapper.selectList(wrapper);
        return Result.ok(messages);
    }
}
