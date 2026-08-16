package org.example.aisprinboot.DTO.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户登录响应 DTO
 *
 * @author PANJU
 */
@Data
@Builder
public class UserLoginResponseDTO {

    /**
     * JWT token
     */
    private String token;

    /**
     * 角色类型
     */
    private String roleType;

    /**
     * 用户详情信息
     */
    private UserDetailResponseDTO userInfo;

    /**
     * 用户详情响应 DTO
     */
    @Builder
    @Data
    public static class UserDetailResponseDTO {
        private Long id;
        private String username;
        private String email;
        private String nickname;
        private String avatar;
        private String phone;
        private Integer gender;
        private String genderDisplayName;
        private LocalDate birthday;
        private Integer userType;
        private String userTypeDisplayName;
        private Integer status;
        private String statusDisplayName;
        private String displayName;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
