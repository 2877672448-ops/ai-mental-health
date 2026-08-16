package org.example.aisprinboot.DTO.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建心理疏导会话入参 DTO
 *
 * @author PANJU
 */
@Data
public class ConsultationSessionCreateDTO {

    /**
     * 会话标题
     */
    @Size(max = 200, message = "会话标题最多200个字符")
    private String sessionTitle;

    /**
     * 初始消息
     */
    @NotBlank(message = "初始消息不能为空")
    @Size(max = 2000, message = "初始消息最多2000个字符")
    private String initialMessage;
}
