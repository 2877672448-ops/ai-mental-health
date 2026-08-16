package org.example.aisprinboot.common;

import lombok.Data;

/**
 * 统一响应结果封装类
 *
 * @author PANJU
 */
@Data
public class Result<T> {
    private String code;
    private String msg;
    private T data;

    /**
     * 成功响应（无数据）
     */
    public static <T> Result<T> ok() {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMsg(ResultCode.SUCCESS.getMsg());
        return result;
    }

    /**
     * 成功响应（携带数据），方法重载
     */
    public static <T> Result<T> ok(T data) {
        Result<T> result = ok();
        result.setData(data);
        return result;
    }

    /**
     * 失败响应（默认错误码）
     */
    public static <T> Result<T> error() {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.ERROR.getCode());
        result.setMsg(ResultCode.ERROR.getMsg());
        return result;
    }

    /**
     * 失败响应（自定义错误码、提示信息与额外数据）
     */
    public static <T> Result<T> error(String code, String msg, T data) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }
}
