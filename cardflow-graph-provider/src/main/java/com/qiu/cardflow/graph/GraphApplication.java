package com.qiu.cardflow.graph;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 主类（项目启动入口）
 */

@SpringBootApplication
@EnableDubbo
public class GraphApplication {
    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(GraphApplication.class);
        springApplication.setWebApplicationType(WebApplicationType.NONE);
        springApplication.run(args);
    }
}
