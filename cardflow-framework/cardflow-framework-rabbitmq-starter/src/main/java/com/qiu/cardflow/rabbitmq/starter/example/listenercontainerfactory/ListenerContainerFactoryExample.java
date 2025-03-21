package com.qiu.cardflow.rabbitmq.starter.example.listenercontainerfactory;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.context.annotation.Bean;

public class ListenerContainerFactoryExample {
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
//        factory.setMessageConverter(converter()); // 设置消息转换器
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL); // 手动确认模式
        factory.setPrefetchCount(50); // 预取消息数量
        factory.setConcurrentConsumers(5); // 并发消费者数量
        factory.setMaxConcurrentConsumers(10); // 最大并发消费者数量
        // 可以设置其他属性，例如事务管理器、重试策略等
        return factory;
    }
}
