package org.example.aisprinboot.util;

import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.aisprinboot.DTO.response.UserLoginResponseDTO;
import org.example.aisprinboot.common.ResultCode;
import org.example.aisprinboot.config.SecurityConfig;
import org.example.aisprinboot.enumClass.UserStatus;
import org.example.aisprinboot.service.UserService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * JWT 认证过滤器：校验请求中的 token 并设置 Spring Security 认证上下文
 *
 * @author PANJU
 */
@Slf4j
public class JwtAuthticationFilter extends OncePerRequestFilter {

    @Resource
    private UserService userService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // 使用ServletPath匹配，与Spring Security的requestMatchers保持一致
        String requestUri = request.getServletPath();
        // 检查是否为公开路径
        return SecurityConfig.isPublicPath(requestUri);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        // 1. 提取 JWT token
        String token = JwtTokenUtil.extractTokenFromRequest(request);
        if (StringUtils.hasText(token)) {
            // 2. 验证token并获取用户信息
            JwtTokenUtil.TokenVerificationResult validationResult = JwtTokenUtil.validateToken(token);
            if (validationResult != null && validationResult.isValid()) {
                // 3. 查询用户信息验证用户的状态
                UserLoginResponseDTO.UserDetailResponseDTO user = userService.getUserById(validationResult.getUserId());
                if (user != null && UserStatus.NORMAL.getCode().equals(user.getStatus())) {
                    // 4. 创建Spring Security认证对象
                    List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                            new SimpleGrantedAuthority("ROLE_" + validationResult.getRoleType())
                    );

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            validationResult.getUsername(),
                            null,
                            authorities
                    );

                    // 设置认证信息到Spring Security上下文
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    // 将token存储到请求属性中，供后续业务获取
                    request.setAttribute("jwtToken", token);
                } else {
                    clearSecurityContext();
                    ResponseUtil.writeError(response, ResultCode.TOKEN_ACCESS_FORBIDDEN);
                    return;
                }
            } else {
                clearSecurityContext();
                ResponseUtil.writeError(response, ResultCode.TOKEN_INVALID);
                return;
            }
        } else {
            // 未携带token：清理上下文后放行，由Spring Security授权规则决定放行或拦截
            // （公开路径正常访问，受保护路径由exceptionHandling统一返回A0301）
            clearSecurityContext();
        }
        // 继续过滤器链
        chain.doFilter(request, response);
    }

    /**
     * 清理Spring Security上下文
     */
    private void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }
}
