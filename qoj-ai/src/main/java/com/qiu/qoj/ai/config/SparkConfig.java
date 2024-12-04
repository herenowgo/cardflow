package com.qiu.qoj.ai.config;

import io.github.briqt.spark4j.SparkClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 讯飞星火大模型的配置
 */
@Configuration
@ConfigurationProperties(prefix = "spark-client")
@Data
public class SparkConfig {

    private String appId;

    private String apiKey;

    private String apiSecret;


    /**
     * @return
     */
    @Bean
    public SparkClient sparkClient() {
        SparkClient sparkClient = new SparkClient();
        sparkClient.appid = appId;
        sparkClient.apiKey = apiKey;
        sparkClient.apiSecret = apiSecret;
        return sparkClient;
    }
}