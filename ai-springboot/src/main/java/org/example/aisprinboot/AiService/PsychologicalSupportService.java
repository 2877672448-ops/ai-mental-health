package org.example.aisprinboot.AiService;

import lombok.extern.slf4j.Slf4j;
import org.example.aisprinboot.DTO.command.ConsultationSessionCreateDTO;
import org.example.aisprinboot.DTO.response.ConsultationMessageResponseDTO;
import org.example.aisprinboot.entity.ConsultationSession;
import org.example.aisprinboot.service.ConsultationMessageService;
import org.example.aisprinboot.service.ConsultationSessionService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

/**
 * 心理疏导 AI 服务：会话管理与流式对话
 *
 * @author PANJU
 */
@Slf4j
@Service
public class PsychologicalSupportService {

    @Autowired
    @Qualifier("open-ai")
    private ChatClient chatClient;

    @Autowired
    private ChatMemory chatMemory;

    @Autowired
    private ConsultationSessionService consultationSessionService;

    @Autowired
    private ConsultationMessageService consultationMessageService;

    /**
     * 开启心理疏导会话
     *
     * @param userId    用户ID
     * @param createDTO 创建会话入参
     * @return 流式对话会话信息
     */
    public StructOutPut.StreamChatSession startSession(Long userId, ConsultationSessionCreateDTO createDTO) {
        // 创建数据库会话记录
        ConsultationSession consultationSession = consultationSessionService.createSession(userId, createDTO);

        // 将初始用户消息保存到Message表
        consultationMessageService.saveUserMessage(consultationSession.getId(), createDTO.getInitialMessage(), null);

        // 创建会话信息
        String sessionId = "session_" + consultationSession.getId();
        return new StructOutPut.StreamChatSession(
                sessionId,
                userId,
                createDTO.getInitialMessage(),
                System.currentTimeMillis(),
                System.currentTimeMillis() + 86400000L,
                1,
                "ACTIVE"
        );
    }

    /**
     * 流式心理疏导对话
     *
     * @param sessionId   会话ID（格式：session_xxx）
     * @param userMessage 用户消息
     * @return AI 回复的文本流
     */
    public Flux<String> streamPsychologicalChat(String sessionId, String userMessage) {
        // 创建响应流：sink.next发布数据、sink.complete完成流、sink.error发布错误
        return Flux.create(sink -> {
            Long dbSessionId = extractSessionId(sessionId);
            if (dbSessionId == null) {
                sink.error(new RuntimeException("会话ID格式错误"));
                return;
            }
            // 检查是否为初始消息，避免重复保存
            boolean isInitialMessage = false;
            Integer messageCount = consultationMessageService.getMessageCountBySessionId(dbSessionId);
            if (messageCount == 1) {
                ConsultationMessageResponseDTO lastMessage = consultationMessageService.getLastMessageBySessionId(dbSessionId);
                if (lastMessage != null && lastMessage.getSenderType() == 1 && userMessage.equals(lastMessage.getContent())) {
                    isInitialMessage = true;
                }
            }
            if (!isInitialMessage) {
                // 保存用户消息到数据库
                consultationMessageService.saveUserMessage(dbSessionId, userMessage, null);
            }

            // 对话记忆管理的会话标识
            String conversationId = "conversation_" + sessionId;
            // 用户消息加入对话记忆
            List<Message> userMessages = new ArrayList<>();
            userMessages.add(new UserMessage(userMessage));
            chatMemory.add(conversationId, userMessages);
            // 构建系统提示词
            Prompt prompt = new Prompt(List.of(
                    new SystemMessage(PromptManage.PSYCHOLOGICAL_SUPPORT_SYSTEM_PROMPT)
            ));

            // 用于存储AI完成的响应
            StringBuilder fullResponse = new StringBuilder();

            // 使用chatClient发送消息到大模型
            chatClient.prompt(prompt)
                    .user(userMessage)
                    .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId))
                    .stream()
                    .content()
                    .doOnNext(fragment -> {
                        fullResponse.append(fragment);
                        sink.next(fragment);
                    })
                    .doOnComplete(() -> {
                        String completeRes = fullResponse.toString();
                        // 将AI返回的内容保存到数据库
                        consultationMessageService.saveAiMessage(dbSessionId, completeRes, "openai");
                        // 添加AI回复到chatMemory
                        List<Message> aiMessages = new ArrayList<>();
                        aiMessages.add(new AssistantMessage(completeRes));
                        chatMemory.add(conversationId, aiMessages);

                        sink.complete();
                    })
                    .doOnError(error -> {
                        // 记录大模型调用的真实异常，便于排查（如API Key无效、网络超时等）
                        log.error("AI流式对话失败, sessionId={}", sessionId, error);
                        sink.error(error);
                    })
                    // 订阅并启动流
                    .subscribe();
        });
    }

    /**
     * 从会话标识中提取数据库会话ID
     *
     * @param sessionId 会话标识（格式：session_xxx）
     * @return 数据库会话ID，格式错误返回 null
     */
    public Long extractSessionId(String sessionId) {
        if (sessionId != null && sessionId.startsWith("session_")) {
            String idStr = sessionId.substring("session_".length());
            return Long.parseLong(idStr);
        }
        return null;
    }
}
