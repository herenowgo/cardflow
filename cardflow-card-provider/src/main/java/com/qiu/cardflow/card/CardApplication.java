package com.qiu.cardflow.card;

import java.util.TimeZone;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;

/**
 * 主类（项目启动入口）
 */

@SpringBootApplication
@EnableDubbo
public class CardApplication {
    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(CardApplication.class);
        springApplication.setWebApplicationType(WebApplicationType.NONE);
        springApplication.run(args);
    }
}
