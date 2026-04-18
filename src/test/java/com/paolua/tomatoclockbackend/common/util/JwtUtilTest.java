package com.paolua.tomatoclockbackend.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JwtUtil 单元测试
 * 测试JWT工具类的各项功能
 */
@DisplayName("JwtUtil 单元测试")
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private static final Long TEST_USER_ID = 12345L;
    private static final String TEST_SECRET = "test-secret-key-for-jwt-util-testing-2026";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        jwtUtil.setSecret(TEST_SECRET);
        jwtUtil.setExpiration(7 * 24 * 60 * 60 * 1000L); // 7 days
    }

    @Test
    @DisplayName("测试生成Token")
    void test_generateToken() {
        // When
        String token = jwtUtil.generateToken(TEST_USER_ID);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        // JWT token has 3 parts separated by dots
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    @DisplayName("测试解析有效Token")
    void test_parseToken_valid() {
        // Given
        String token = jwtUtil.generateToken(TEST_USER_ID);

        // When
        Claims claims = jwtUtil.parseToken(token);

        // Then
        assertNotNull(claims);
        assertEquals(TEST_USER_ID, claims.get("userId", Long.class));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    @DisplayName("测试解析无效Token - 错误的密钥")
    void test_parseToken_invalid_wrongSecret() {
        // Given - create token with different secret
        SecretKey differentKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        String invalidToken = Jwts.builder()
                .claim("userId", TEST_USER_ID)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(differentKey)
                .compact();

        // When & Then
        assertThrows(Exception.class, () -> jwtUtil.parseToken(invalidToken));
    }

    @Test
    @DisplayName("测试解析无效Token - 错误的格式")
    void test_parseToken_invalid_format() {
        // Given
        String invalidToken = "invalid.token.format";

        // When & Then
        assertThrows(Exception.class, () -> jwtUtil.parseToken(invalidToken));
    }

    @Test
    @DisplayName("测试解析无效Token - 空Token")
    void test_parseToken_invalid_null() {
        // When & Then
        assertThrows(Exception.class, () -> jwtUtil.parseToken(null));
    }

    @Test
    @DisplayName("测试从Token获取用户ID")
    void test_getUserIdFromToken() {
        // Given
        String token = jwtUtil.generateToken(TEST_USER_ID);

        // When
        Long userId = jwtUtil.getUserIdFromToken(token);

        // Then
        assertEquals(TEST_USER_ID, userId);
    }

    @Test
    @DisplayName("测试判断Token未过期")
    void test_isTokenExpired_notExpired() {
        // Given
        String token = jwtUtil.generateToken(TEST_USER_ID);

        // When
        boolean isExpired = jwtUtil.isTokenExpired(token);

        // Then
        assertFalse(isExpired);
    }

    @Test
    @DisplayName("测试判断Token已过期")
    void test_isTokenExpired_expired() {
        // Given - create an expired token
        jwtUtil.setExpiration(-1000L); // Set negative expiration
        String expiredToken = jwtUtil.generateToken(TEST_USER_ID);

        // When
        boolean isExpired = jwtUtil.isTokenExpired(expiredToken);

        // Then
        assertTrue(isExpired);
    }

    @Test
    @DisplayName("测试Token过期时间")
    void test_token_expiration() {
        // Given
        jwtUtil.setExpiration(3600000L); // 1 hour
        String token = jwtUtil.generateToken(TEST_USER_ID);
        Claims claims = jwtUtil.parseToken(token);

        // When
        Date issuedAt = claims.getIssuedAt();
        Date expiration = claims.getExpiration();

        // Then
        assertNotNull(issuedAt);
        assertNotNull(expiration);
        long diff = expiration.getTime() - issuedAt.getTime();
        assertEquals(3600000L, diff);
    }

    @Test
    @DisplayName("测试刷新Token")
    void test_refreshToken() {
        // Given
        String oldToken = jwtUtil.generateToken(TEST_USER_ID);

        // When
        String newToken = jwtUtil.refreshToken(oldToken);

        // Then
        assertNotNull(newToken);
        assertNotEquals(oldToken, newToken);
        assertEquals(TEST_USER_ID, jwtUtil.getUserIdFromToken(newToken));
    }

    @Test
    @DisplayName("测试刷新过期Token")
    void test_refreshExpiredToken() {
        // Given - create expired token
        jwtUtil.setExpiration(-1000L);
        String expiredToken = jwtUtil.generateToken(TEST_USER_ID);

        // Reset to normal expiration
        jwtUtil.setExpiration(7 * 24 * 60 * 60 * 1000L);

        // When & Then - refreshing expired token should fail
        assertThrows(Exception.class, () -> jwtUtil.refreshToken(expiredToken));
    }

    @Test
    @DisplayName("测试Token包含正确的Claims")
    void test_token_claims() {
        // Given
        String token = jwtUtil.generateToken(TEST_USER_ID);
        Claims claims = jwtUtil.parseToken(token);

        // When & Then
        assertEquals(TEST_USER_ID, claims.get("userId", Long.class));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    @DisplayName("测试生成多个Token有不同的IssuedAt")
    void test_multipleTokens_differentIssuedAt() {
        // When
        String token1 = jwtUtil.generateToken(TEST_USER_ID);
        // Small delay to ensure different timestamps
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        String token2 = jwtUtil.generateToken(TEST_USER_ID);

        // Then
        Claims claims1 = jwtUtil.parseToken(token1);
        Claims claims2 = jwtUtil.parseToken(token2);

        assertNotEquals(token1, token2);
        assertNotEquals(claims1.getIssuedAt(), claims2.getIssuedAt());
    }

    @Test
    @DisplayName("测试不同用户的Token")
    void test_differentUsers() {
        // Given
        Long userId1 = 1001L;
        Long userId2 = 1002L;

        // When
        String token1 = jwtUtil.generateToken(userId1);
        String token2 = jwtUtil.generateToken(userId2);

        // Then
        assertNotEquals(token1, token2);
        assertEquals(userId1, jwtUtil.getUserIdFromToken(token1));
        assertEquals(userId2, jwtUtil.getUserIdFromToken(token2));
    }
}
