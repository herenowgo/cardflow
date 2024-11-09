# qoj-eventStream模块

## 主要功能

主要用来管理SSE连接，并且通过接受消息来给对应的连接中发送信息
对外暴露接口返回一个SSE连接给SpringWebMVC服务，并且通过监听消息队列中的消息，来给对应的连接发送数据

## 技术栈

1. Spring WebFlux （https://docs.spring.io/spring-framework/reference/web/webflux.html）
2. SpringCloudStream （https://docs.spring.io/spring-cloud-stream/reference/index.html）

## 版本
0.1