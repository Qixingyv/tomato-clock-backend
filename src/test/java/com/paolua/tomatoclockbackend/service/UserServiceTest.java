package com.paolua.tomatoclockbackend.service;

import com.paolua.tomatoclockbackend.common.exception.BusinessException;
import com.paolua.tomatoclockbackend.common.response.ResultCode;
import com.paolua.tomatoclockbackend.mapper.UserMapper;
import com.paolua.tomatoclockbackend.pojo.User;
import com.paolua.tomatoclockbackend.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserService 单元测试
 * 测试用户服务的各项功能
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 单元测试")
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private UserServiceImpl userService;

    private static final String TEST_WECHAT_OPENID = "test_wechat_openid_12345";
    private static final String TEST_HUAWEI_UID = "test_huawei_uid_67890";
    private static final String TEST_CODE = "test_auth_code";

    @BeforeEach
    void setUp() {
        // 设置配置属性
        ReflectionTestUtils.setField(userService, "wechatAppId", "test_appid");
        ReflectionTestUtils.setField(userService, "wechatSecret", "test_secret");
        ReflectionTestUtils.setField(userService, "huaweiClientId", "test_client_id");
        ReflectionTestUtils.setField(userService, "huaweiClientSecret", "test_client_secret");
    }

    @Test
    @DisplayName("测试微信登录新用户")
    void test_wechatLogin_newUser() {
        // Given - 模拟微信API返回成功响应
        Map<String, Object> wechatResponse = new HashMap<>();
        wechatResponse.put("openid", TEST_WECHAT_OPENID);
        wechatResponse.put("access_token", "test_access_token");
        wechatResponse.put("expires_in", 7200);

        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenReturn(wechatResponse);
        when(userMapper.selectByWechatOpenid(TEST_WECHAT_OPENID))
                .thenReturn(null);
        when(userMapper.insert(any(User.class)))
                .thenReturn(1);

        // When
        User result = userService.wechatLogin(TEST_CODE);

        // Then
        assertNotNull(result);
        assertEquals(TEST_WECHAT_OPENID, result.getWechatOpenid());
        assertTrue(result.getNickname().contains("微信用户"));
        assertNotNull(result.getCreateTime());
        assertNotNull(result.getUpdateTime());

        // 验证调用
        verify(userMapper).selectByWechatOpenid(TEST_WECHAT_OPENID);
        verify(userMapper).insert(any(User.class));
    }

    @Test
    @DisplayName("测试微信登录老用户")
    void test_wechatLogin_existingUser() {
        // Given - 模拟现有用户
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setWechatOpenid(TEST_WECHAT_OPENID);
        existingUser.setNickname("老用户");
        existingUser.setCreateTime(LocalDateTime.now().minusDays(30));
        existingUser.setUpdateTime(LocalDateTime.now().minusDays(30));

        Map<String, Object> wechatResponse = new HashMap<>();
        wechatResponse.put("openid", TEST_WECHAT_OPENID);

        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenReturn(wechatResponse);
        when(userMapper.selectByWechatOpenid(TEST_WECHAT_OPENID))
                .thenReturn(existingUser);

        // When
        User result = userService.wechatLogin(TEST_CODE);

        // Then
        assertNotNull(result);
        assertEquals(existingUser.getId(), result.getId());
        assertEquals(TEST_WECHAT_OPENID, result.getWechatOpenid());
        assertEquals("老用户", result.getNickname());

        // 验证没有插入新用户
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    @DisplayName("测试华为登录新用户")
    void test_huaweiLogin_newUser() {
        // Given - 模拟华为API返回成功响应
        Map<String, Object> tokenResponse = new HashMap<>();
        tokenResponse.put("access_token", "test_access_token");
        tokenResponse.put("token_type", "Bearer");
        tokenResponse.put("expires_in", 3600);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("sub", TEST_HUAWEI_UID);
        userInfo.put("name", "测试用户");
        userInfo.put("email", "test@example.com");

        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenReturn(tokenResponse);
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenReturn(userInfo);
        when(userMapper.selectByHuaweiUid(TEST_HUAWEI_UID))
                .thenReturn(null);
        when(userMapper.insert(any(User.class)))
                .thenReturn(1);

        // When
        User result = userService.huaweiLogin(TEST_CODE);

        // Then
        assertNotNull(result);
        assertEquals(TEST_HUAWEI_UID, result.getHuaweiUid());
        assertEquals("测试用户", result.getNickname());
        assertNotNull(result.getCreateTime());
        assertNotNull(result.getUpdateTime());

        // 验证调用
        verify(userMapper).selectByHuaweiUid(TEST_HUAWEI_UID);
        verify(userMapper).insert(any(User.class));
    }

    @Test
    @DisplayName("测试华为登录老用户")
    void test_huaweiLogin_existingUser() {
        // Given - 模拟现有用户
        User existingUser = new User();
        existingUser.setId(2L);
        existingUser.setHuaweiUid(TEST_HUAWEI_UID);
        existingUser.setNickname("华为老用户");
        existingUser.setCreateTime(LocalDateTime.now().minusDays(15));
        existingUser.setUpdateTime(LocalDateTime.now().minusDays(15));

        Map<String, Object> tokenResponse = new HashMap<>();
        tokenResponse.put("access_token", "test_access_token");

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("sub", TEST_HUAWEI_UID);
        userInfo.put("name", "华为老用户");

        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenReturn(tokenResponse);
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenReturn(userInfo);
        when(userMapper.selectByHuaweiUid(TEST_HUAWEI_UID))
                .thenReturn(existingUser);

        // When
        User result = userService.huaweiLogin(TEST_CODE);

        // Then
        assertNotNull(result);
        assertEquals(existingUser.getId(), result.getId());
        assertEquals(TEST_HUAWEI_UID, result.getHuaweiUid());
        assertEquals("华为老用户", result.getNickname());

        // 验证没有插入新用户
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    @DisplayName("测试根据ID获取用户")
    void test_getUserById() {
        // Given
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setNickname("测试用户");

        when(userMapper.selectById(userId)).thenReturn(user);

        // When
        User result = userService.getUserById(userId);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("测试用户", result.getNickname());
    }

    @Test
    @DisplayName("测试根据ID获取用户 - 用户不存在")
    void test_getUserById_notFound() {
        // Given
        Long userId = 999L;
        when(userMapper.selectById(userId)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.getUserById(userId);
        });

        assertEquals(ResultCode.USER_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("测试更新用户信息")
    void test_updateUser() {
        // Given
        User user = new User();
        user.setId(1L);
        user.setNickname("更新后的昵称");
        user.setPhone("13800138000");

        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // When
        User result = userService.updateUser(user);

        // Then
        assertNotNull(result);
        assertEquals("更新后的昵称", result.getNickname());
        assertNotNull(result.getUpdateTime());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).updateById(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertNotNull(capturedUser.getUpdateTime());
    }

    @Test
    @DisplayName("测试更新用户信息 - 更新失败")
    void test_updateUser_failure() {
        // Given
        User user = new User();
        user.setId(1L);
        user.setNickname("测试用户");

        when(userMapper.updateById(any(User.class))).thenReturn(0);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.updateUser(user);
        });

        assertEquals("用户更新失败", exception.getMessage());
    }

    @Test
    @DisplayName("测试无效微信授权码")
    void test_invalidWechatAuthCode() {
        // Given - 模拟微信API返回错误
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("errcode", 40029);
        errorResponse.put("errmsg", "invalid code");

        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenReturn(errorResponse);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.wechatLogin("invalid_code");
        });

        assertEquals(ResultCode.USER_LOGIN_FAILED.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("测试无效华为授权码")
    void test_invalidHuaweiAuthCode() {
        // Given - 模拟华为API返回错误
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "invalid_request");
        errorResponse.put("error_description", "Invalid code");

        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenReturn(errorResponse);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.huaweiLogin("invalid_code");
        });

        assertEquals(ResultCode.USER_LOGIN_FAILED.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("测试华为登录无昵称时的默认处理")
    void test_huaweiLogin_withoutNickname() {
        // Given - 用户信息不包含name字段
        Map<String, Object> tokenResponse = new HashMap<>();
        tokenResponse.put("access_token", "test_access_token");

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("sub", TEST_HUAWEI_UID);
        // 不包含name字段

        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenReturn(tokenResponse);
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenReturn(userInfo);
        when(userMapper.selectByHuaweiUid(TEST_HUAWEI_UID))
                .thenReturn(null);
        when(userMapper.insert(any(User.class)))
                .thenReturn(1);

        // When
        User result = userService.huaweiLogin(TEST_CODE);

        // Then
        assertNotNull(result);
        assertEquals(TEST_HUAWEI_UID, result.getHuaweiUid());
        assertEquals("华为用户", result.getNickname());
    }

    @Test
    @DisplayName("测试用户创建失败")
    void test_userCreationFailure() {
        // Given
        Map<String, Object> wechatResponse = new HashMap<>();
        wechatResponse.put("openid", TEST_WECHAT_OPENID);

        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenReturn(wechatResponse);
        when(userMapper.selectByWechatOpenid(TEST_WECHAT_OPENID))
                .thenReturn(null);
        when(userMapper.insert(any(User.class)))
                .thenReturn(0); // 插入失败

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.wechatLogin(TEST_CODE);
        });

        assertEquals("用户创建失败", exception.getMessage());
    }
}
