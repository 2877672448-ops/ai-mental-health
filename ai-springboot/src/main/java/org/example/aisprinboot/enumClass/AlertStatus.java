package org.example.aisprinboot.enumClass;

import lombok.Getter;

/**
 * 危机预警处理状态枚举
 *
 * @author PANJU
 */
@Getter
public enum AlertStatus {

    /**
     * 未处理
     */
    PENDING(0, "未处理"),
    /**
     * 已处理
     */
    HANDLED(1, "已处理"),
    /**
     * 已忽略
     */
    IGNORED(2, "已忽略");

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 状态描述
     */
    private final String description;

    AlertStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据代码获取枚举
     *
     * @param code 状态代码
     * @return 对应的枚举项
     */
    public static AlertStatus fromCode(Integer code) {
        for (AlertStatus status : AlertStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的预警状态代码: " + code);
    }

    /**
     * 验证状态代码是否有效
     *
     * @param code 状态代码
     * @return 是否为有效的状态代码
     */
    public static boolean isValidCode(Integer code) {
        for (AlertStatus status : AlertStatus.values()) {
            if (status.getCode().equals(code)) {
                return true;
            }
        }
        return false;
    }
}
