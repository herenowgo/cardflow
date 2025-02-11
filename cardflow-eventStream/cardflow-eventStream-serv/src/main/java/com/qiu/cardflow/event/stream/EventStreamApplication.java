package com.qiu.cardflow.event.stream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = "com.qiu.cardflow")
@EnableDiscoveryClient
public class EventStreamApplication {
    public static void main(String[] args) {
        SpringApplication.run(EventStreamApplication.class, args);
    }
} 