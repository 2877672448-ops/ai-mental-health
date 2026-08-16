package org.example.aisprinboot.exception;

import lombok.Getter;

/**
 * 业务异常类
 *
 * @author PANJU
 */
@Getter
public class BusionessException extends RuntimeException {
    /**
     * 错误码
     */
    private final String code;

    /**
     * 错误信息
     */
    private final String message;

    /**
     * 携带的额外数据
     */
    private final Object data;

    public BusionessException(String message) {
        super(message);
        this.code = "BUSINESS_ERROR";
        this.message = message;
        this.data = null;
    }

    public BusionessException(String code, String message, Object data) {
        super(message);
        this.code = code;
        this.message = message;
        this.data = data;
    }
}
