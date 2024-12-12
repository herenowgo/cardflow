package com.qiu.qoj.document;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 主类（项目启动入口）
 */

@SpringBootApplication(scanBasePackages = "com.qiu.qoj")
@EnableDiscoveryClient
public class DocumentApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocumentApplication.class, args);
    }

}
