package com.qiu.qoj.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

//@SpringBootApplication(scanBasePackages = "com.qiu.qoj")
@MapperScan("com.qiu.qoj.user.mapper")
////@EnableScheduling
////@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
////@ComponentScan("com.qiu")
////@EnableDiscoveryClient
//@EnableDiscoveryClient
////@EnableFeignClients(basePackages = {"com.qiu.qojbackendserviceclient.service"})
@SpringBootApplication(scanBasePackages = "com.qiu.qoj")
@EnableDiscoveryClient
public class UserApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }

}
