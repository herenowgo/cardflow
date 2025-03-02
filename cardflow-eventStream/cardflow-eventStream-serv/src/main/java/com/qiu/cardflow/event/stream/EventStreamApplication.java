package com.qiu.cardflow.event.stream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.qiu.cardflow")
@EnableDiscoveryClient
@EnableScheduling
public class EventStreamApplication {
    public static void main(String[] args) {
        SpringApplication.run(EventStreamApplication.class, args);
    }
}