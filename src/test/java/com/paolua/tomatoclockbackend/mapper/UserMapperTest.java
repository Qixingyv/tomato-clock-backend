package com.paolua.tomatoclockbackend.mapper;

import com.paolua.tomatoclockbackend.pojo.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserMapper 单元测试
 * 测试用户Mapper的SQL操作
 */
@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("UserMapper 单元测试")
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        testUser = new User();
        testUser.setPhone("13800138000");
        testUser.setWechatOpenid("test_openid_123");
        testUser.setHuaweiUid("test_huawei_uid_456");
        testUser.setNickname("测试用户");
        testUser.setAvatar("https://example.com/avatar.jpg");
        testUser.setCreateTime(LocalDateTime.now());
        testUser.setUpdateTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("测试插入用户")
    void test_insert() {
        // When
        int result = userMapper.insert(testUser);

        // Then
        assertTrue(result > 0);
        assertNotNull(testUser.getId());

        // 验证插入后的数据
        User insertedUser = userMapper.selectById(testUser.getId());
        assertNotNull(insertedUser);
        assertEquals("测试用户", insertedUser.getNickname());
        assertEquals("13800138000", insertedUser.getPhone());
    }

    @Test
    @DisplayName("测试根据ID查询用户")
    void test_selectById() {
        // Given - 先插入用户
        userMapper.insert(testUser);

        // When
        User result = userMapper.selectById(testUser.getId());

        // Then
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getNickname(), result.getNickname());
        assertEquals(testUser.getPhone(), result.getPhone());
    }

    @Test
    @DisplayName("测试根据ID查询不存在的用户")
    void test_selectById_notFound() {
        // When
        User result = userMapper.selectById(999999L);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("测试根据微信OpenID查询用户")
    void test_selectByWechatOpenid() {
        // Given - 先插入用户
        userMapper.insert(testUser);

        // When
        User result = userMapper.selectByWechatOpenid("test_openid_123");

        // Then
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals("test_openid_123", result.getWechatOpenid());
    }

    @Test
    @DisplayName("测试根据微信OpenID查询不存在的用户")
    void test_selectByWechatOpenid_notFound() {
        // When
        User result = userMapper.selectByWechatOpenid("nonexistent_openid");

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("测试根据华为UID查询用户")
    void test_selectByHuaweiUid() {
        // Given - 先插入用户
        userMapper.insert(testUser);

        // When
        User result = userMapper.selectByHuaweiUid("test_huawei_uid_456");

        // Then
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals("test_huawei_uid_456", result.getHuaweiUid());
    }

    @Test
    @DisplayName("测试根据华为UID查询不存在的用户")
    void test_selectByHuaweiUid_notFound() {
        // When
        User result = userMapper.selectByHuaweiUid("nonexistent_huawei_uid");

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("测试根据手机号查询用户")
    void test_selectByPhone() {
        // Given - 先插入用户
        userMapper.insert(testUser);

        // When
        User result = userMapper.selectByPhone("13800138000");

        // Then
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals("13800138000", result.getPhone());
    }

    @Test
    @DisplayName("测试根据手机号查询不存在的用户")
    void test_selectByPhone_notFound() {
        // When
        User result = userMapper.selectByPhone("19999999999");

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("测试更新用户信息")
    void test_updateById() {
        // Given - 先插入用户
        userMapper.insert(testUser);
        Long userId = testUser.getId();

        // When - 更新用户信息
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setNickname("更新后的昵称");
        updateUser.setAvatar("https://example.com/new-avatar.jpg");
        updateUser.setUpdateTime(LocalDateTime.now());

        int result = userMapper.updateById(updateUser);

        // Then
        assertTrue(result > 0);

        // 验证更新后的数据
        User updatedUser = userMapper.selectById(userId);
        assertEquals("更新后的昵称", updatedUser.getNickname());
        assertEquals("https://example.com/new-avatar.jpg", updatedUser.getAvatar());
    }

    @Test
    @DisplayName("测试微信OpenID唯一性")
    void test_wechatOpenid_uniqueness() {
        // Given - 插入第一个用户
        User user1 = new User();
        user1.setWechatOpenid("same_openid");
        user1.setNickname("用户1");
        user1.setCreateTime(LocalDateTime.now());
        user1.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user1);

        // When - 尝试插入相同OpenID的用户
        User user2 = new User();
        user2.setWechatOpenid("same_openid");
        user2.setNickname("用户2");
        user2.setCreateTime(LocalDateTime.now());
        user2.setUpdateTime(LocalDateTime.now());

        // Then - 应该抛出异常或返回0（取决于数据库配置）
        // 这里验证行为取决于数据库约束配置
        assertThrows(Exception.class, () -> {
            userMapper.insert(user2);
        });
    }

    @Test
    @DisplayName("测试华为UID唯一性")
    void test_huaweiUid_uniqueness() {
        // Given - 插入第一个用户
        User user1 = new User();
        user1.setHuaweiUid("same_huawei_uid");
        user1.setNickname("用户1");
        user1.setCreateTime(LocalDateTime.now());
        user1.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user1);

        // When - 尝试插入相同华为UID的用户
        User user2 = new User();
        user2.setHuaweiUid("same_huawei_uid");
        user2.setNickname("用户2");
        user2.setCreateTime(LocalDateTime.now());
        user2.setUpdateTime(LocalDateTime.now());

        // Then - 应该抛出异常或返回0
        assertThrows(Exception.class, () -> {
            userMapper.insert(user2);
        });
    }

    @Test
    @DisplayName("测试更新不存在的用户")
    void test_updateById_notFound() {
        // When
        User updateUser = new User();
        updateUser.setId(999999L);
        updateUser.setNickname("不存在的用户");
        updateUser.setUpdateTime(LocalDateTime.now());

        int result = userMapper.updateById(updateUser);

        // Then - 返回0表示没有更新任何记录
        assertEquals(0, result);
    }

    @Test
    @DisplayName("测试部分字段更新")
    void test_updateById_partialFields() {
        // Given - 先插入用户
        testUser.setWechatOpenid("partial_update_test");
        userMapper.insert(testUser);
        Long userId = testUser.getId();
        String originalPhone = testUser.getPhone();

        // When - 只更新昵称
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setNickname("只更新昵称");
        updateUser.setUpdateTime(LocalDateTime.now());

        userMapper.updateById(updateUser);

        // Then - 验证昵称已更新，其他字段保持不变
        User updatedUser = userMapper.selectById(userId);
        assertEquals("只更新昵称", updatedUser.getNickname());
        assertEquals(originalPhone, updatedUser.getPhone());
    }
}
