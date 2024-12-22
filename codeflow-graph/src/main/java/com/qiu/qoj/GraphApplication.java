package com.qiu.qoj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = "com.qiu.qoj")
@EnableDiscoveryClient
public class GraphApplication {
    public static void main(String[] args) {
        SpringApplication.run(GraphApplication.class, args);
    }
}