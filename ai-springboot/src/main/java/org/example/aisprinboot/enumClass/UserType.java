package org.example.aisprinboot.enumClass;

import lombok.Getter;

/**
 * 用户类型枚举
 *
 * @author PANJU
 */
@Getter
public enum UserType {

    /**
     * 普通用户
     */
    USER(1, "普通用户"),
    /**
     * 管理员
     */
    ADMIN(2, "管理员");

    /**
     * 类型码
     */
    private final Integer code;

    /**
     * 类型描述
     */
    private final String description;

    UserType(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据代码获取枚举
     *
     * @param code 类型代码
     * @return 对应的枚举项
     */
    public static UserType fromCode(Integer code) {
        for (UserType type : UserType.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的用户类型代码: " + code);
    }

    /**
     * 验证用户类型代码是否有效
     *
     * @param code 类型代码
     * @return 是否为有效的用户类型
     */
    public static boolean isValidCode(Integer code) {
        for (UserType type : UserType.values()) {
            if (type.getCode().equals(code)) {
                return true;
            }
        }
        return false;
    }
}
