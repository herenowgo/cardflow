package com.qiu.cardflow.redis.starter.key;

import org.springframework.context.annotation.Configuration;

/**
 * @Author idea
 * @Date: Created in 15:58 2023/5/17
 * @Description
 */
@Configuration
public class AICacheKeyBuilder extends RedisKeyBuilder {

    private static String MODEL_USAGE = "model:usage";

    public String buildModelUsageKey(Long userId, String aiModelName) {
        return super.getPrefix() + MODEL_USAGE + super.getSplitItem() + userId + super.getSplitItem() + aiModelName;
    }

}
