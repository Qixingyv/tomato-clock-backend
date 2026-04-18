package com.paolua.tomatoclockbackend.service.impl;

import com.paolua.tomatoclockbackend.common.exception.BusinessException;
import com.paolua.tomatoclockbackend.common.response.ResultCode;
import com.paolua.tomatoclockbackend.mapper.UserMapper;
import com.paolua.tomatoclockbackend.pojo.User;
import com.paolua.tomatoclockbackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户服务实现类
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final RestTemplate restTemplate;

    @Value("${wechat.appid}")
    private String wechatAppId;

    @Value("${wechat.secret}")
    private String wechatSecret;

    @Value("${huawei.client-id}")
    private String huaweiClientId;

    @Value("${huawei.client-secret}")
    private String huaweiClientSecret;

    public UserServiceImpl(UserMapper userMapper, RestTemplate restTemplate) {
        this.userMapper = userMapper;
        this.restTemplate = restTemplate;
    }

    @Override
    public User wechatLogin(String code) {
        log.info("微信登录请求，code: {}", code);

        try {
            // 调用微信API获取openid和access_token
            String url = "https://api.weixin.qq.com/sns/oauth2/access_token?" +
                    "appid=" + wechatAppId +
                    "&secret=" + wechatSecret +
                    "&code=" + code +
                    "&grant_type=authorization_code";

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null || response.containsKey("errcode")) {
                log.error("微信登录失败: {}", response);
                throw new BusinessException(ResultCode.USER_LOGIN_FAILED);
            }

            String openid = (String) response.get("openid");

            // 查询用户是否存在
            User user = userMapper.selectByWechatOpenid(openid);

            if (user == null) {
                // 新用户，创建用户
                user = new User();
                user.setWechatOpenid(openid);
                user.setNickname("微信用户" + openid.substring(openid.length() - 6));
                user.setCreateTime(LocalDateTime.now());
                user.setUpdateTime(LocalDateTime.now());

                int result = userMapper.insert(user);
                if (result <= 0) {
                    throw new BusinessException("用户创建失败");
                }

                log.info("新用户注册成功: {}", openid);
            } else {
                log.info("现有用户登录: {}", openid);
            }

            return user;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("微信登录异常", e);
            throw new BusinessException(ResultCode.USER_LOGIN_FAILED);
        }
    }

    @Override
    public User huaweiLogin(String code) {
        log.info("华为登录请求，code: {}", code);

        try {
            // 调用华为API获取access_token
            String tokenUrl = "https://oauth-login.cloud.huawei.com/oauth2/v3/token";

            Map<String, String> tokenRequest = new HashMap<>();
            tokenRequest.put("grant_type", "authorization_code");
            tokenRequest.put("code", code);
            tokenRequest.put("client_id", huaweiClientId);
            tokenRequest.put("client_secret", huaweiClientSecret);
            tokenRequest.put("redirect_uri", "");

            @SuppressWarnings("unchecked")
            Map<String, Object> tokenResponse = restTemplate.postForObject(tokenUrl, tokenRequest, Map.class);

            if (tokenResponse == null || !tokenResponse.containsKey("access_token")) {
                log.error("华为登录获取token失败: {}", tokenResponse);
                throw new BusinessException(ResultCode.USER_LOGIN_FAILED);
            }

            String accessToken = (String) tokenResponse.get("access_token");

            // 获取用户信息
            String userInfoUrl = "https://account.cloud.huawei.com/restful/userInfo?access_token=" + accessToken;

            @SuppressWarnings("unchecked")
            Map<String, Object> userInfo = restTemplate.getForObject(userInfoUrl, Map.class);

            if (userInfo == null || !userInfo.containsKey("sub")) {
                log.error("华为登录获取用户信息失败: {}", userInfo);
                throw new BusinessException(ResultCode.USER_LOGIN_FAILED);
            }

            String huaweiUid = (String) userInfo.get("sub");
            String nickname = userInfo.containsKey("name") ? (String) userInfo.get("name") : "华为用户";

            // 查询用户是否存在
            User user = userMapper.selectByHuaweiUid(huaweiUid);

            if (user == null) {
                // 新用户，创建用户
                user = new User();
                user.setHuaweiUid(huaweiUid);
                user.setNickname(nickname);
                user.setCreateTime(LocalDateTime.now());
                user.setUpdateTime(LocalDateTime.now());

                int result = userMapper.insert(user);
                if (result <= 0) {
                    throw new BusinessException("用户创建失败");
                }

                log.info("新用户注册成功: {}", huaweiUid);
            } else {
                log.info("现有用户登录: {}", huaweiUid);
            }

            return user;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("华为登录异常", e);
            throw new BusinessException(ResultCode.USER_LOGIN_FAILED);
        }
    }

    @Override
    public User getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return user;
    }

    @Override
    public User updateUser(User user) {
        user.setUpdateTime(LocalDateTime.now());
        int result = userMapper.updateById(user);
        if (result <= 0) {
            throw new BusinessException("用户更新失败");
        }
        return user;
    }
}
