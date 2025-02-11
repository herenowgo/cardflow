package com.qiu.cardflow.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

//@SpringBootApplication(scanBasePackages = "com.qiu.cardflow")
@MapperScan("com.qiu.cardflow.user.mapper")
////@EnableScheduling
////@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
////@ComponentScan("com.qiu")
////@EnableDiscoveryClient
//@EnableDiscoveryClient
////@EnableFeignClients(basePackages = {"com.qiu.cardflowbackendserviceclient.service"})
@SpringBootApplication(scanBasePackages = "com.qiu.cardflow")
@EnableDiscoveryClient
public class UserApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }

}
