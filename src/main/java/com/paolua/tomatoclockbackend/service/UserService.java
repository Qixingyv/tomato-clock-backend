package com.paolua.tomatoclockbackend.service;

import com.paolua.tomatoclockbackend.pojo.User;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 微信登录
     * 根据微信授权码获取用户信息，新用户自动注册
     *
     * @param code 微信授权码
     * @return 用户信息
     */
    User wechatLogin(String code);

    /**
     * 华为登录
     * 根据华为授权码获取用户信息，新用户自动注册
     *
     * @param code 华为授权码
     * @return 用户信息
     */
    User huaweiLogin(String code);

    /**
     * 根据用户ID获取用户信息
     *
     * @param id 用户ID
     * @return 用户信息
     */
    User getUserById(Long id);

    /**
     * 更新用户信息
     *
     * @param user 用户信息
     * @return 更新后的用户信息
     */
    User updateUser(User user);
}
