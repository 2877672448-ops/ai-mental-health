package org.example.aisprinboot.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI ChatClient 配置：对话记忆 + 系统人设
 *
 * @author PANJU
 */
@Configuration
public class ChatClientConfig {

    /**
     * 对话记忆：保留最新的30条消息
     */
    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .maxMessages(30)
                .build();
    }

    /**
     * OpenAI 兼容模型的 ChatClient（注入容器中的ChatMemory Bean，避免重复创建实例）
     * 用于心理疏导对话：带记忆、带"心理疏导师"人设
     */
    @Bean("open-ai")
    public ChatClient openAiChatClient(OpenAiChatModel openAiChatModel, ChatMemory chatMemory) {
        return ChatClient.builder(openAiChatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .defaultSystem("你是一个专业的心理疏导师，温和耐心，善于倾听，能够提供专业的心理支持和建议")
                .build();
    }

    /**
     * 危机分析专用 ChatClient：无默认人设、无记忆 Advisor
     * 避免复用 open-ai 的"心理疏导师"人设导致分析结果用聊天口吻，且避免记忆 Advisor 串用不同用户数据（隐私风险）
     * 用于：结构化风险评估、日记深度分析
     */
    @Bean("analysis-ai")
    public ChatClient analysisChatClient(OpenAiChatModel openAiChatModel) {
        return ChatClient.builder(openAiChatModel).build();
    }
}
