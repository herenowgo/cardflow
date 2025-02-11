package com.qiu.cardflow.ai.manager.zhipuai;

import com.zhipu.oapi.ClientV4;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class ZhipuManager {

    @Value("${zhipuai.secretKey}")
    private String secretKey;

    @Bean
    public ClientV4 getZhipuai() {
        ClientV4 client = new ClientV4.Builder(secretKey)
                .enableTokenCache()
                .networkConfig(30, 10, 10, 10, TimeUnit.SECONDS)
                .connectionPool(new okhttp3.ConnectionPool(8, 1, TimeUnit.SECONDS))
                .build();

        return client;
    }

}
