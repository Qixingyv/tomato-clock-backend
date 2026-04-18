package com.paolua.tomatoclockbackend.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 用于生成和解析JWT Token
 * 支持从配置文件读取密钥和过期时间
 */
@Component
@Slf4j
public class JwtUtil {

    /**
     * JWT密钥（从配置文件读取）
     * 生产环境应使用足够长度的密钥（建议256位以上）
     */
    @Value("${jwt.secret:tomato-clock-secret-key-for-production-use-2026-spring-boot-application}")
    private String secret;

    /**
     * Token过期时间（毫秒）
     * 默认7天
     */
    @Value("${jwt.expiration:604800000}")
    private Long expiration;

    /**
     * 是否在每次启动时生成新密钥（开发环境使用）
     */
    @Value("${jwt.generate-key-on-start:false}")
    private boolean generateKeyOnStart;

    /**
     * 实际使用的密钥
     */
    private SecretKey key;

    /**
     * 初始化密钥
     */
    @PostConstruct
    public void init() {
        if (generateKeyOnStart) {
            // 开发环境：每次启动生成新密钥
            this.key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
            log.info("Generated new JWT key for development");
        } else {
            // 生产环境：使用配置的密钥
            try {
                // 确保密钥长度足够（HS512需要至少64字节）
                byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
                if (keyBytes.length < 64) {
                    // 密钥太短，进行base64扩展
                    String base64Key = Base64.getEncoder().encodeToString(keyBytes);
                    while (base64Key.getBytes(StandardCharsets.UTF_8).length < 64) {
                        base64Key += base64Key;
                    }
                    keyBytes = base64Key.getBytes(StandardCharsets.UTF_8);
                }
                this.key = Keys.hmacShaKeyFor(keyBytes);
                log.info("Initialized JWT with configured secret");
            } catch (Exception e) {
                log.error("Failed to initialize JWT key, generating new one", e);
                this.key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
            }
        }
    }

    /**
     * 生成Token
     *
     * @param userId 用户ID
     * @return JWT Token
     */
    public String generateToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        return createToken(claims);
    }

    /**
     * 创建Token
     *
     * @param claims 自定义声明
     * @return JWT Token
     */
    private String createToken(Map<String, Object> claims) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 解析Token
     *
     * @param token JWT Token
     * @return Claims对象
     * @throws io.jsonwebtoken.JwtException 如果Token无效或过期
     */
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从Token中获取用户ID
     *
     * @param token JWT Token
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }

    /**
     * 判断Token是否过期
     *
     * @param token JWT Token
     * @return true=已过期，false=未过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return true;
        }
    }

    /**
     * 刷新Token
     *
     * @param token 旧Token
     * @return 新Token
     */
    public String refreshToken(String token) {
        Claims claims = parseToken(token);
        Long userId = claims.get("userId", Long.class);
        return generateToken(userId);
    }

    /**
     * 获取Token的过期时间
     *
     * @param token JWT Token
     * @return 过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getExpiration();
    }

    /**
     * 验证Token是否有效（未过期且格式正确）
     *
     * @param token JWT Token
     * @return true=有效，false=无效
     */
    public boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取Token的签发时间
     *
     * @param token JWT Token
     * @return 签发时间
     */
    public Date getIssuedAtDateFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getIssuedAt();
    }

    /**
     * 设置密钥（用于测试）
     *
     * @param secret 密钥
     */
    public void setSecret(String secret) {
        this.secret = secret;
        init();
    }

    /**
     * 设置过期时间（用于测试）
     *
     * @param expiration 过期时间（毫秒）
     */
    public void setExpiration(Long expiration) {
        this.expiration = expiration;
    }

    /**
     * 获取当前配置的过期时间
     *
     * @return 过期时间（毫秒）
     */
    public Long getExpiration() {
        return expiration;
    }
}
