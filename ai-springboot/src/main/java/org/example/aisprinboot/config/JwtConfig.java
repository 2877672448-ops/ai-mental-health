package org.example.aisprinboot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置类，绑定 application.yml 中的 jwt 前缀配置
 *
 * @author PANJU
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    /**
     * 签名密钥
     */
    private String secret;

    /**
     * 过期时间（毫秒）
     */
    private long expiration;

    /**
     * 刷新过期时间（毫秒）
     */
    private long refreshExpiration;

    /**
     * token 头部名称
     */
    private String header;

    /**
     * token 前缀
     */
    private String tokenPrefix;
}
