package com.qiu.qoj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

//@SpringBootApplication(scanBasePackages = "com.qiu.qoj")
////@MapperScan("com.qiu.qoj.mapper")
////@EnableScheduling
////@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
////@ComponentScan("com.qiu")
////@EnableDiscoveryClient
//@EnableDiscoveryClient
////@EnableFeignClients(basePackages = {"com.qiu.qojbackendserviceclient.service"})
@SpringBootApplication
@EnableDiscoveryClient
public class QojBackendUserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(QojBackendUserServiceApplication.class, args);
    }

}
