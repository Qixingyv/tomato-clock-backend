package com.paolua.tomatoclockbackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 微信登录请求DTO
 */
@Data
public class WechatLoginRequest {

    /**
     * 微信授权码
     */
    @NotBlank(message = "授权码不能为空")
    private String code;
}
