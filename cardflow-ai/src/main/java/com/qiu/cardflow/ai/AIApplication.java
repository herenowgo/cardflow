package com.qiu.cardflow.ai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 主类（项目启动入口）
 */

@SpringBootApplication(scanBasePackages = "com.qiu.cardflow")
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.qiu.cardflow.ai.mapper")
public class AIApplication {

    public static void main(String[] args) {
        SpringApplication.run(AIApplication.class, args);
    }

}
