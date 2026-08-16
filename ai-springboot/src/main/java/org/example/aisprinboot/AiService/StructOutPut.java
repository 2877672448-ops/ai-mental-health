package org.example.aisprinboot.AiService;

/**
 * AI 结构化输出封装
 *
 * @author PANJU
 */
public class StructOutPut {

    /**
     * 流式对话会话信息
     */
    public record StreamChatSession(
            String sessionId,
            Long userHash,
            String initialMessage,
            Long startTime,
            Long expiryTime,
            Integer messageCount,
            String status
    ) {
    }
}
