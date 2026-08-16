package org.example.aisprinboot.util;

import jakarta.servlet.http.HttpServletResponse;
import org.example.aisprinboot.common.Result;
import org.example.aisprinboot.common.ResultCode;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * 响应写出工具类，用于在过滤器中直接写出 JSON 错误响应
 *
 * @author PANJU
 */
@Slf4j
public class ResponseUtil {

    /**
     * 在过滤器中写出错误响应
     *
     * @param response   HTTP 响应
     * @param resultCode 结果码枚举
     */
    public static void writeError(HttpServletResponse response, ResultCode resultCode) {
        // 根据不同结果码返回不同的HTTP状态
        int status = switch (resultCode) {
            case UNAUTHORIZED, ACCESS_UNAUTHORIZED, TOKEN_INVALID, TOKEN_EXPIRED, TOKEN_BLOCKED -> HttpStatus.UNAUTHORIZED.value();
            case TOKEN_ACCESS_FORBIDDEN -> HttpStatus.FORBIDDEN.value();
            default -> HttpStatus.BAD_REQUEST.value();
        };
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        try (PrintWriter writer = response.getWriter()) {
            String jsonResponse = JSONUtil.toJsonStr(Result.error(resultCode.getCode(), resultCode.getMsg(), null));
            writer.print(jsonResponse);
            // 确保将响应内容写入到输出流
            writer.flush();
        } catch (IOException e) {
            log.error("写入响应失败：{}", e.getMessage());
        }
    }
}
