package com.paolua.tomatoclockbackend.dto;

import com.paolua.tomatoclockbackend.pojo.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /**
     * JWT Token
     */
    private String token;

    /**
     * Token类型（固定为Bearer）
     */
    private String tokenType = "Bearer";

    /**
     * 过期时间（毫秒）
     */
    private Long expiresIn;

    /**
     * 用户信息
     */
    private User userInfo;

    public LoginResponse(String token, User userInfo) {
        this.token = token;
        this.userInfo = userInfo;
    }

    public LoginResponse(String token, Long expiresIn, User userInfo) {
        this.token = token;
        this.expiresIn = expiresIn;
        this.userInfo = userInfo;
    }
}
