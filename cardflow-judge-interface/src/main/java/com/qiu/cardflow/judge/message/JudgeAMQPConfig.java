package com.qiu.cardflow.judge.message;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class JudgeAMQPConfig {

    public static final String JUDGE_EXCHANGE = "judge.exchange";
    public static final String JUDGE_QUEUE = "judge.queue";

    @Bean
    Exchange judgeExchange() {
        return ExchangeBuilder.topicExchange(JUDGE_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    Queue judgeQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", JudgeDeadLetterConfig.DEAD_LETTER_EXCHANGE); // 指定 DLX
        args.put("x-dead-letter-routing-key", JudgeDeadLetterConfig.DEAD_LETTER_ROUTING_KEY); // 指定 DLQ 的路由键

        return new Queue(JUDGE_QUEUE, true, false, false, args);
    }

    @Bean
    Binding judgeBinding(Queue judgeQueue, Exchange judgeExchange) {
        return BindingBuilder.bind(judgeQueue)
                .to(judgeExchange)
                .with("#")
                .noargs();
    }
}
