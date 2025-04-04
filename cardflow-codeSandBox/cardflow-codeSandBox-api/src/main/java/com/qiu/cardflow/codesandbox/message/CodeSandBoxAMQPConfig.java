package com.qiu.cardflow.codesandbox.message;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CodeSandBoxAMQPConfig {

    @Bean
    Exchange codeSandBoxExchange() {
        return ExchangeBuilder.topicExchange("codesandbox.exchange")
                .durable(true)
                .build();
    }

    @Bean
    Queue codeSandBoxToEventStreamQueue() {
        return new Queue("codesandboxToEventStream.queue", true, false, false);
    }

    @Bean
    Binding codeSandBoxBinding(Queue codeSandBoxToEventStreamQueue, Exchange codeSandBoxExchange) {
        return BindingBuilder.bind(codeSandBoxToEventStreamQueue)
                .to(codeSandBoxExchange)
                .with("toEventStream")
                .noargs();
    }
}
