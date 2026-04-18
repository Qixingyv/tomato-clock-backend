package com.paolua.tomatoclockbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paolua.tomatoclockbackend.common.exception.BusinessException;
import com.paolua.tomatoclockbackend.common.response.ResultCode;
import com.paolua.tomatoclockbackend.common.util.JwtUtil;
import com.paolua.tomatoclockbackend.dto.HuaweiLoginRequest;
import com.paolua.tomatoclockbackend.dto.LoginResponse;
import com.paolua.tomatoclockbackend.dto.UserDTO;
import com.paolua.tomatoclockbackend.dto.WechatLoginRequest;
import com.paolua.tomatoclockbackend.pojo.User;
import com.paolua.tomatoclockbackend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserController 单元测试
 * 测试用户控制器的HTTP接口
 */
@WebMvcTest(UserController.class)
@DisplayName("UserController 单元测试")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

    private static final String TEST_TOKEN = "Bearer test_jwt_token_12345";
    private static final Long TEST_USER_ID = 1L;

    @BeforeEach
    void setUp() {
        // Mock JWT工具类行为
        when(jwtUtil.generateToken(anyLong())).thenReturn("mock_jwt_token");
        when(jwtUtil.getExpiration()).thenReturn(7 * 24 * 60 * 60 * 1000L);
        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(TEST_USER_ID);
        when(jwtUtil.isTokenExpired(anyString())).thenReturn(false);
    }

    @Test
    @DisplayName("测试微信登录成功")
    void test_wechatLogin_success() throws Exception {
        // Given
        WechatLoginRequest request = new WechatLoginRequest();
        request.setCode("test_wechat_code");

        User mockUser = new User();
        mockUser.setId(TEST_USER_ID);
        mockUser.setNickname("微信用户");
        mockUser.setWechatOpenid("test_openid");
        mockUser.setCreateTime(LocalDateTime.now());

        when(userService.wechatLogin("test_wechat_code")).thenReturn(mockUser);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/wechat/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("mock_jwt_token"))
                .andExpect(jsonPath("$.data.user.id").value(TEST_USER_ID))
                .andExpect(jsonPath("$.data.user.nickname").value("微信用户"));

        verify(userService).wechatLogin("test_wechat_code");
        verify(jwtUtil).generateToken(TEST_USER_ID);
    }

    @Test
    @DisplayName("测试微信登录失败")
    void test_wechatLogin_failure() throws Exception {
        // Given
        WechatLoginRequest request = new WechatLoginRequest();
        request.setCode("invalid_code");

        when(userService.wechatLogin("invalid_code"))
                .thenThrow(new BusinessException(ResultCode.USER_LOGIN_FAILED));

        // When & Then
        mockMvc.perform(post("/api/v1/auth/wechat/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ResultCode.USER_LOGIN_FAILED.getCode()));

        verify(userService).wechatLogin("invalid_code");
        verify(jwtUtil, never()).generateToken(anyLong());
    }

    @Test
    @DisplayName("测试华为登录成功")
    void test_huaweiLogin_success() throws Exception {
        // Given
        HuaweiLoginRequest request = new HuaweiLoginRequest();
        request.setCode("test_huawei_code");

        User mockUser = new User();
        mockUser.setId(TEST_USER_ID);
        mockUser.setNickname("华为用户");
        mockUser.setHuaweiUid("test_huawei_uid");
        mockUser.setCreateTime(LocalDateTime.now());

        when(userService.huaweiLogin("test_huawei_code")).thenReturn(mockUser);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/huawei/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("mock_jwt_token"))
                .andExpect(jsonPath("$.data.user.id").value(TEST_USER_ID))
                .andExpect(jsonPath("$.data.user.nickname").value("华为用户"));

        verify(userService).huaweiLogin("test_huawei_code");
        verify(jwtUtil).generateToken(TEST_USER_ID);
    }

    @Test
    @DisplayName("测试华为登录失败")
    void test_huaweiLogin_failure() throws Exception {
        // Given
        HuaweiLoginRequest request = new HuaweiLoginRequest();
        request.setCode("invalid_code");

        when(userService.huaweiLogin("invalid_code"))
                .thenThrow(new BusinessException(ResultCode.USER_LOGIN_FAILED));

        // When & Then
        mockMvc.perform(post("/api/v1/auth/huawei/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ResultCode.USER_LOGIN_FAILED.getCode()));

        verify(userService).huaweiLogin("invalid_code");
        verify(jwtUtil, never()).generateToken(anyLong());
    }

    @Test
    @DisplayName("测试验证Token有效")
    void test_validateToken_valid() throws Exception {
        // Given
        User mockUser = new User();
        mockUser.setId(TEST_USER_ID);
        mockUser.setPhone("13800138000");
        mockUser.setNickname("测试用户");
        mockUser.setAvatar("https://example.com/avatar.jpg");
        mockUser.setCreateTime(LocalDateTime.now());

        when(userService.getUserById(TEST_USER_ID)).thenReturn(mockUser);

        // When & Then
        mockMvc.perform(get("/api/v1/auth/validate")
                        .header("Authorization", TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(TEST_USER_ID))
                .andExpect(jsonPath("$.data.phone").value("13800138000"))
                .andExpect(jsonPath("$.data.nickname").value("测试用户"));

        verify(jwtUtil).getUserIdFromToken("test_jwt_token_12345");
        verify(userService).getUserById(TEST_USER_ID);
    }

    @Test
    @DisplayName("测试验证Token无效 - 缺少Authorization header")
    void test_validateToken_missingHeader() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/auth/validate"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("测试刷新Token")
    void test_refreshToken() throws Exception {
        // Given
        String newToken = "new_jwt_token_67890";
        when(jwtUtil.refreshToken("test_jwt_token_12345")).thenReturn(newToken);

        User mockUser = new User();
        mockUser.setId(TEST_USER_ID);
        mockUser.setNickname("测试用户");
        mockUser.setCreateTime(LocalDateTime.now());

        when(userService.getUserById(TEST_USER_ID)).thenReturn(mockUser);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .header("Authorization", TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value(newToken));

        verify(jwtUtil).refreshToken("test_jwt_token_12345");
    }

    @Test
    @DisplayName("测试刷新Token - Token已过期")
    void test_refreshToken_expired() throws Exception {
        // Given
        when(jwtUtil.isTokenExpired("test_jwt_token_12345")).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .header("Authorization", TEST_TOKEN))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ResultCode.USER_TOKEN_EXPIRED.getCode()));

        verify(jwtUtil).isTokenExpired("test_jwt_token_12345");
        verify(jwtUtil, never()).refreshToken(anyString());
    }

    @Test
    @DisplayName("测试登出")
    void test_logout() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(jwtUtil).getUserIdFromToken("test_jwt_token_12345");
    }

    @Test
    @DisplayName("测试获取当前用户信息")
    void test_getCurrentUser() throws Exception {
        // Given
        User mockUser = new User();
        mockUser.setId(TEST_USER_ID);
        mockUser.setPhone("13800138000");
        mockUser.setNickname("测试用户");
        mockUser.setAvatar("https://example.com/avatar.jpg");
        mockUser.setCreateTime(LocalDateTime.now());

        when(userService.getUserById(TEST_USER_ID)).thenReturn(mockUser);

        // When & Then
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(TEST_USER_ID))
                .andExpect(jsonPath("$.data.nickname").value("测试用户"))
                .andExpect(jsonPath("$.data.phone").value("13800138000"))
                .andExpect(jsonPath("$.data.avatar").value("https://example.com/avatar.jpg"));

        verify(jwtUtil).getUserIdFromToken("test_jwt_token_12345");
        verify(userService).getUserById(TEST_USER_ID);
    }

    @Test
    @DisplayName("测试微信登录参数校验 - code为空")
    void test_wechatLogin_validation_emptyCode() throws Exception {
        // Given
        WechatLoginRequest request = new WechatLoginRequest();
        request.setCode("");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/wechat/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).wechatLogin(anyString());
    }

    @Test
    @DisplayName("测试华为登录参数校验 - code为空")
    void test_huaweiLogin_validation_emptyCode() throws Exception {
        // Given
        HuaweiLoginRequest request = new HuaweiLoginRequest();
        request.setCode("");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/huawei/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).huaweiLogin(anyString());
    }

    @Test
    @DisplayName("测试Authorization header格式错误")
    void test_authorizationHeader_invalidFormat() throws Exception {
        // Given - 使用不正确的格式（缺少Bearer前缀）
        when(jwtUtil.getUserIdFromToken("invalid_format_token"))
                .thenThrow(new RuntimeException("Invalid token"));

        // When & Then
        mockMvc.perform(get("/api/v1/auth/validate")
                        .header("Authorization", "invalid_format_token"))
                .andExpect(status().isBadRequest());
    }
}
