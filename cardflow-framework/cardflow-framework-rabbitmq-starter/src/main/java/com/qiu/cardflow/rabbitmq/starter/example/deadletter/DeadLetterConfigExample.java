package com.qiu.cardflow.rabbitmq.starter.example.deadletter;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;

/**
 * RabbitMQ 死信队列配置类模板代码
 */
public class DeadLetterConfigExample {

    // 死信队列的名称
    public static final String DEAD_LETTER_QUEUE = "dead.letter.queue";

    // 死信交换机的名称
    public static final String DEAD_LETTER_EXCHANGE = "dead.letter.exchange";

    // 死信路由键
    public static final String DEAD_LETTER_ROUTING_KEY = "dead.letter.routingkey";

    // 死信队列
    @Bean
    public Queue deadLetterQueue() {
        return new Queue(DEAD_LETTER_QUEUE, true); // 持久化
    }

    // 死信交换机
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE, true, false); // 持久化, 非自动删除
    }

    // 绑定死信队列和死信交换机
    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with(DEAD_LETTER_ROUTING_KEY);
    }
}
