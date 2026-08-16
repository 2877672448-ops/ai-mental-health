package org.example.aisprinboot.common;

import lombok.extern.slf4j.Slf4j;
import org.example.aisprinboot.exception.BusionessException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 *
 * @author PANJU
 */
@Slf4j
@RestControllerAdvice
public class GlobarExceptionHandeler {

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<String> handlerException(MethodArgumentNotValidException e) {
        // 汇总所有字段校验失败的提示信息
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return Result.error(ResultCode.PARAM_ERROR.getCode(), message, null);
    }

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusionessException.class)
    public Result<?> handleBusinessException(BusionessException e) {
        // 如果异常携带额外的数据，一并返回
        return Result.error(e.getCode(), e.getMessage(), e.getData());
    }

    /**
     * 处理路径不存在异常（如浏览器直接访问根路径/），返回友好的404提示而非500
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<String> handleNoResourceFoundException(NoResourceFoundException e) {
        return Result.error(ResultCode.NOT_FOUND.getCode(),
                "接口路径不存在，请检查请求地址，测试可访问 /api/text", null);
    }
    
    /**
     * 处理其他未知异常，兜底返回系统错误
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error(ResultCode.SYSTEM_ERROR.getCode(), ResultCode.SYSTEM_ERROR.getMsg(), null);
    }
}
