package com.paolua.tomatoclockbackend.mapper;

import com.paolua.tomatoclockbackend.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户Mapper接口
 * 处理用户相关的数据库操作
 */
@Mapper
public interface UserMapper {

    /**
     * 根据微信OpenID查询用户
     *
     * @param wechatOpenid 微信OpenID
     * @return 用户信息，不存在则返回null
     */
    @Select("SELECT * FROM user WHERE wechat_openid = #{wechatOpenid} LIMIT 1")
    User selectByWechatOpenid(@Param("wechatOpenid") String wechatOpenid);

    /**
     * 根据华为UID查询用户
     *
     * @param huaweiUid 华为UID
     * @return 用户信息，不存在则返回null
     */
    @Select("SELECT * FROM user WHERE huawei_uid = #{huaweiUid} LIMIT 1")
    User selectByHuaweiUid(@Param("huaweiUid") String huaweiUid);

    /**
     * 根据手机号查询用户
     *
     * @param phone 手机号
     * @return 用户信息，不存在则返回null
     */
    @Select("SELECT * FROM user WHERE phone = #{phone} LIMIT 1")
    User selectByPhone(@Param("phone") String phone);

    /**
     * 根据用户ID查询用户
     *
     * @param id 用户ID
     * @return 用户信息，不存在则返回null
     */
    @Select("SELECT * FROM user WHERE id = #{id}")
    User selectById(@Param("id") Long id);

    /**
     * 插入用户
     *
     * @param user 用户信息
     * @return 影响行数
     */
    int insert(User user);

    /**
     * 更新用户信息
     *
     * @param user 用户信息
     * @return 影响行数
     */
    int updateById(User user);
}
