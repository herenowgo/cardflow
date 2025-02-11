package com.qiu.cardflow.gateway.config;

import cn.dev33.satoken.jwt.StpLogicJwtForMixin;
import cn.dev33.satoken.stp.StpLogic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @auther macrozheng
 * @description Sa-Token相关配置
 * @date 2023/11/28
 * @github https://github.com/macrozheng
 */
@Configuration
public class SaTokenConfig {

    /**
     * 采用 JWT 和 Session 的混合模式
     *
     * @return
     */
    @Bean
    public StpLogic getStpLogicJwt() {
        return new StpLogicJwtForMixin();
    }

}

