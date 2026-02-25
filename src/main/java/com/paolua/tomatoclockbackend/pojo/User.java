package com.paolua.tomatoclockbackend.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户核心身份信息表
 * 对应数据库表：user
 *
 * @author Paolua
 * @date 2026-02-23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /**
     * 用户唯一标识（自增）
     */
    private Long id;

    /**
     * 手机号（登录用），唯一
     */
    private String phone;

    /**
     * 微信OpenID（登录用），唯一
     */
    private String wechatOpenid;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户头像URL
     */
    private String avatar;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间（自动更新）
     */
    private LocalDateTime updateTime;
}