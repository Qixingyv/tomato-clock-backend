package com.paolua.tomatoclockbackend.config;

import org.apache.ibatis.session.Configuration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;

/**
 * MyBatis配置类
 */
@org.springframework.context.annotation.Configuration
@MapperScan("com.paolua.tomatoclockbackend.mapper")
public class MyBatisConfig {

    /**
     * MyBatis配置
     * 开启驼峰命名转换
     */
    @Bean
    public Configuration mybatisConfiguration() {
        Configuration configuration = new Configuration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setUseGeneratedKeys(true);
        return configuration;
    }
}
