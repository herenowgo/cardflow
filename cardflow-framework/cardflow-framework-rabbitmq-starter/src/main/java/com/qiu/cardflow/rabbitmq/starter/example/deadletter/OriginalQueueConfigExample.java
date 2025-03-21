package com.qiu.cardflow.rabbitmq.starter.example.deadletter;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;

/**
 * 原始队列绑定死信队列的模板代码
 */
public class OriginalQueueConfigExample {

    // 原始队列的名称
    public static final String ORIGINAL_QUEUE = "original.queue";

    // 原始交换机的名称
    public static final String ORIGINAL_EXCHANGE = "original.exchange";

    // 原始路由键
    public static final String ORIGINAL_ROUTING_KEY = "original.routingkey";

    @Bean
    public Queue originalQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", DeadLetterConfigExample.DEAD_LETTER_EXCHANGE); // 指定 DLX
        args.put("x-dead-letter-routing-key", DeadLetterConfigExample.DEAD_LETTER_ROUTING_KEY); // 指定 DLQ 的路由键

        // x-message-ttl 可以设置消息过期时间（毫秒）
        // args.put("x-message-ttl", 10000);

        return new Queue(ORIGINAL_QUEUE, true, false, false, args); // 持久化，设置死信策略
    }

    @Bean
    public DirectExchange originalExchange() {
        return new DirectExchange(ORIGINAL_EXCHANGE, true, false); // 持久化, 非自动删除
    }

    @Bean
    public Binding originalBinding() {
        return BindingBuilder.bind(originalQueue()).to(originalExchange()).with(ORIGINAL_ROUTING_KEY);
    }
}
