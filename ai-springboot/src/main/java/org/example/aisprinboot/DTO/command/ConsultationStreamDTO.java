package org.example.aisprinboot.DTO.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 心理疏导流式对话入参 DTO
 *
 * @author PANJU
 */
@Data
public class ConsultationStreamDTO {

    /**
     * 会话ID
     */
    @NotBlank(message = "sessionId不能为空")
    private String sessionId;

    /**
     * 用户消息
     */
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 2000, message = "消息长度不能超过2000个字符")
    private String userMessage;
}
