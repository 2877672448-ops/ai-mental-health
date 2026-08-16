package org.example.aisprinboot.config;

import cn.hutool.core.text.AntPathMatcher;
import jakarta.servlet.http.HttpServletResponse;
import org.example.aisprinboot.common.Result;
import org.example.aisprinboot.common.ResultCode;
import org.example.aisprinboot.util.JwtAuthticationFilter;
import cn.hutool.json.JSONUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置：无状态会话 + JWT 认证过滤器
 *
 * @author PANJU
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final AntPathMatcher antPathMatcher = new AntPathMatcher();

    /**
     * 公开路径，无需登录即可访问
     */
    private static final String[] PUBLIC_PATHS = {
            "/",
            "/error",
            "/*.html",
            "/api/text",
            "/api/user/login",
            "/api/user/add",
            // 知识库 - 公开接口
            "/api/knowledge/category/tree",
            "/api/knowledge/article/page",
            "/api/knowledge/article/*"
    };

    /**
     * 判断请求路径是否为公开路径
     *
     * @param requestUri 请求URI
     * @return 是否为公开路径
     */
    public static boolean isPublicPath(String requestUri) {
        for (String publicPath : PUBLIC_PATHS) {
            if (antPathMatcher.match(publicPath, requestUri)) {
                return true;
            }
        }
        return false;
    }

    /**
     * JWT 认证过滤器
     */
    @Bean
    public JwtAuthticationFilter jwtAuthticationFilter() {
        return new JwtAuthticationFilter();
    }

    /**
     * 安全过滤器链配置
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 禁用CSRF保护：纯JWT无状态API，认证依赖自定义token头而非Cookie，无需CSRF防护
                .csrf(AbstractHttpConfigurer::disable)
                // 配置会话管理为无状态（JWT需要）
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 配置请求的授权规则
                .authorizeHttpRequests(auth -> auth
                        // 公开的路径，无需登录即可访问
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        // 其他请求都需要认证
                        .anyRequest().authenticated()
                )
                // 未认证访问受保护接口时，统一返回JSON格式的A0301错误（而非默认的HTML错误页）
                .exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(JSONUtil.toJsonStr(
                            Result.error(ResultCode.ACCESS_UNAUTHORIZED.getCode(), ResultCode.ACCESS_UNAUTHORIZED.getMsg(), null)));
                }))
                // 添加JWT认证过滤器
                .addFilterBefore(jwtAuthticationFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
