package com.qiu.qoj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 主类（项目启动入口）
 */

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class AIApplication {

    public static void main(String[] args) {
        SpringApplication.run(AIApplication.class, args);
    }

}
