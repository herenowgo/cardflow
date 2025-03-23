package com.qiu.cardflow.judge.message;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class JudgeDeadLetterConfig {
    // 死信队列的名称
    public static final String DEAD_LETTER_QUEUE = "judge.dead.letter.queue";

    // 死信交换机的名称
    public static final String DEAD_LETTER_EXCHANGE = "judge.dead.letter.exchange";

    // 死信路由键
    public static final String DEAD_LETTER_ROUTING_KEY = "judge.dead.letter.routingkey";

    // 死信队列
    @Bean
    public Queue judgeDeadLetterQueue() {
        return new Queue(DEAD_LETTER_QUEUE, true); // 持久化
    }

    // 死信交换机
    @Bean
    public DirectExchange judgeDeadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE, true, false); // 持久化, 非自动删除
    }

    // 绑定死信队列和死信交换机
    @Bean
    public Binding deadLetterBinding(Queue judgeDeadLetterQueue, DirectExchange judgeDeadLetterExchange) {
        return BindingBuilder.bind(judgeDeadLetterQueue).to(judgeDeadLetterExchange).with(DEAD_LETTER_ROUTING_KEY);
    }

//    // 配置监听器容器工厂，设置重试策略
//    @Bean
//    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory2(ConnectionFactory connectionFactory, RetryTemplate retryTemplate) {
//        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
//        factory.setConnectionFactory(connectionFactory);
//        factory.setMessageConverter(new Jackson2JsonMessageConverter());
//        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
//        factory.setPrefetchCount(5); // 预取消息数量
//        factory.setConcurrentConsumers(3); // 并发消费者数量
//        factory.setMaxConcurrentConsumers(3); // 最大并发消费者数量
//
//        // 配置重试模板
//        factory.setRetryTemplate(retryTemplate);
//
//        return factory;
//    }
//
//    @Bean
//    public RetryTemplate retryTemplate() {
//        RetryTemplate retryTemplate = new RetryTemplate();
//
//        // 配置重试策略 - 重试1次
//        RetryPolicy retryPolicy = new SimpleRetryPolicy(2); // 总共执行2次，即重试1次
//        retryTemplate.setRetryPolicy(retryPolicy);
//
//        // 配置重试间隔
//        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
//        backOffPolicy.setBackOffPeriod(1000); // 重试间隔1秒
//        retryTemplate.setBackOffPolicy(backOffPolicy);
//
//        return retryTemplate;
//    }
}
