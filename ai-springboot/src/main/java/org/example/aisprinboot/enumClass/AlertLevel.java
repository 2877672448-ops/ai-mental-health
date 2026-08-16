package org.example.aisprinboot.enumClass;

import lombok.Getter;

/**
 * 危机预警级别枚举
 *
 * @author PANJU
 */
@Getter
public enum AlertLevel {

    /**
     * 低危
     */
    LOW(1, "低危"),
    /**
     * 中危
     */
    MEDIUM(2, "中危"),
    /**
     * 高危
     */
    HIGH(3, "高危");

    /**
     * 级别码
     */
    private final Integer code;

    /**
     * 级别描述
     */
    private final String description;

    AlertLevel(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据代码获取枚举
     *
     * @param code 级别代码
     * @return 对应的枚举项
     */
    public static AlertLevel fromCode(Integer code) {
        for (AlertLevel level : AlertLevel.values()) {
            if (level.getCode().equals(code)) {
                return level;
            }
        }
        throw new IllegalArgumentException("未知的预警级别代码: " + code);
    }
}
