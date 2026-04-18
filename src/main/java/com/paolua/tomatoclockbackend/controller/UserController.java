package com.paolua.tomatoclockbackend.controller;

import com.paolua.tomatoclockbackend.common.exception.BusinessException;
import com.paolua.tomatoclockbackend.common.response.Result;
import com.paolua.tomatoclockbackend.common.response.ResultCode;
import com.paolua.tomatoclockbackend.common.util.JwtUtil;
import com.paolua.tomatoclockbackend.dto.HuaweiLoginRequest;
import com.paolua.tomatoclockbackend.dto.LoginResponse;
import com.paolua.tomatoclockbackend.dto.UserDTO;
import com.paolua.tomatoclockbackend.dto.WechatLoginRequest;
import com.paolua.tomatoclockbackend.pojo.User;
import com.paolua.tomatoclockbackend.security.annotation.SkipAuth;
import com.paolua.tomatoclockbackend.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户认证控制器
 * 处理用户登录、登出、Token验证等操作
 */
@RestController
@RequestMapping("/api/v1/auth")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 微信登录
     *
     * @param request 登录请求
     * @return 登录响应，包含Token和用户信息
     */
    @SkipAuth
    @PostMapping("/wechat/login")
    public Result<LoginResponse> wechatLogin(@Valid @RequestBody WechatLoginRequest request) {
        log.info("微信登录请求: code={}", request.getCode());

        User user = userService.wechatLogin(request.getCode());
        String token = jwtUtil.generateToken(user.getId());

        LoginResponse response = new LoginResponse(token, jwtUtil.getExpiration(), user);

        log.info("微信登录成功: userId={}", user.getId());
        return Result.success(response);
    }

    /**
     * 华为登录
     *
     * @param request 登录请求
     * @return 登录响应，包含Token和用户信息
     */
    @SkipAuth
    @PostMapping("/huawei/login")
    public Result<LoginResponse> huaweiLogin(@Valid @RequestBody HuaweiLoginRequest request) {
        log.info("华为登录请求: code={}", request.getCode());

        User user = userService.huaweiLogin(request.getCode());
        String token = jwtUtil.generateToken(user.getId());

        LoginResponse response = new LoginResponse(token, jwtUtil.getExpiration(), user);

        log.info("华为登录成功: userId={}", user.getId());
        return Result.success(response);
    }

    /**
     * 验证Token
     *
     * @param authHeader Authorization header
     * @return 用户信息
     */
    @GetMapping("/validate")
    public Result<UserDTO> validateToken(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);

        User user = userService.getUserById(userId);

        UserDTO userDTO = new UserDTO(
                user.getId(),
                user.getPhone(),
                user.getNickname(),
                user.getAvatar(),
                user.getCreateTime()
        );

        return Result.success(userDTO);
    }

    /**
     * 刷新Token
     *
     * @param authHeader Authorization header
     * @return 新Token
     */
    @PostMapping("/refresh")
    public Result<LoginResponse> refreshToken(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");

        if (jwtUtil.isTokenExpired(token)) {
            throw new BusinessException(ResultCode.USER_TOKEN_EXPIRED);
        }

        String newToken = jwtUtil.refreshToken(token);
        Long userId = jwtUtil.getUserIdFromToken(newToken);
        User user = userService.getUserById(userId);

        LoginResponse response = new LoginResponse(newToken, jwtUtil.getExpiration(), user);

        return Result.success(response);
    }

    /**
     * 登出
     *
     * @param authHeader Authorization header
     * @return 成功响应
     */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);

        log.info("用户登出: userId={}", userId);

        // 由于使用的是无状态JWT，登出主要在客户端处理（删除Token）
        // 如果需要服务端登出，可以添加Token黑名单机制
        return Result.success();
    }

    /**
     * 获取当前用户信息
     *
     * @param authHeader Authorization header
     * @return 用户信息
     */
    @GetMapping("/me")
    public Result<UserDTO> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);

        User user = userService.getUserById(userId);

        UserDTO userDTO = new UserDTO(
                user.getId(),
                user.getPhone(),
                user.getNickname(),
                user.getAvatar(),
                user.getCreateTime()
        );

        return Result.success(userDTO);
    }
}
