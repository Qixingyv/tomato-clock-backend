package com.paolua.tomatoclockbackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 华为登录请求DTO
 */
@Data
public class HuaweiLoginRequest {

    /**
     * 华为授权码
     */
    @NotBlank(message = "授权码不能为空")
    private String code;
}
