package org.example.aisprinboot.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import org.example.aisprinboot.DTO.command.ConsultationSessionCreateDTO;
import org.example.aisprinboot.entity.ConsultationSession;
import org.example.aisprinboot.entity.User;
import org.example.aisprinboot.exception.BusionessException;
import org.example.aisprinboot.mapper.ConsultationSessionMapper;
import org.example.aisprinboot.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 咨询会话业务层
 *
 * @author PANJU
 */
@Service
public class ConsultationSessionService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ConsultationSessionMapper consultationSessionMapper;

    /**
     * 创建咨询会话
     *
     * @param userId    用户ID
     * @param createDTO 创建会话入参
     * @return 创建的会话实体
     */
    public ConsultationSession createSession(Long userId, ConsultationSessionCreateDTO createDTO) {
        // 验证用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusionessException("用户不存在");
        }

        // 创建会话记录
        ConsultationSession session = ConsultationSession.builder()
                .userId(userId)
                .sessionTitle(createDTO.getSessionTitle())
                .startedAt(LocalDateTime.now())
                .build();
        // 如果未提供标题，使用默认标题
        if (StrUtil.isBlank(createDTO.getSessionTitle())) {
            session.setSessionTitle("宁渡AI助手 - " + DateUtil.format(LocalDateTime.now(), "MM-dd HH:mm"));
        }

        // 插入记录
        consultationSessionMapper.insert(session);
        return session;
    }
}
