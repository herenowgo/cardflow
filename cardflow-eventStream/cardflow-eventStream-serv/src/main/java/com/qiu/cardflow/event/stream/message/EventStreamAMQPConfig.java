package com.qiu.cardflow.event.stream.message;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Random;

import static com.qiu.codeflow.eventStream.message.EventStreamMessageConstant.EVENT_STREAM_EXCHANGE;

@Component
public class EventStreamAMQPConfig {

    public static final String EVENT_STREAM_QUEUE;

    static {
        String ipAddress = "unknown";
        try {
            ipAddress = java.net.InetAddress.getLocalHost().getHostAddress();
        } catch (java.net.UnknownHostException e) {
            // fallback to random UUID if IP cannot be determined
        }
        EVENT_STREAM_QUEUE = "eventStream.queue." + ipAddress + "." + new Random().nextInt(10000);
    }

    @Bean
    public Queue eventStreamQueue() {
        return new Queue(EVENT_STREAM_QUEUE, true, true, true);
    }

    @Bean
    public Exchange eventStreamExchange() {
        return ExchangeBuilder.topicExchange(EVENT_STREAM_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Binding eventStreamBinding(Exchange eventStreamExchange, Queue eventStreamQueue) {
        return BindingBuilder.bind(eventStreamQueue)
                .to(eventStreamExchange)
                .with(EVENT_STREAM_QUEUE)
                .noargs();
    }
}
