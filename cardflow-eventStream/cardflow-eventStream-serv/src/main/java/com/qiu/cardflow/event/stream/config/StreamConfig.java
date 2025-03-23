package com.qiu.cardflow.event.stream.config;

import com.qiu.cardflow.event.stream.message.EventStreamAMQPConfig;
import com.qiu.cardflow.event.stream.service.EventStreamService;
import com.qiu.codeflow.eventStream.dto.EventMessage;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.DirectMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class StreamConfig implements ApplicationRunner {

    private final EventStreamService eventStreamService;

//    @Bean("eventMessage")
//    public Consumer<EventMessage> processEventMessage() {
//        return eventStreamService::pushEvent;
//    }

//    @RabbitListener()
//    public void processEventMessage(EventMessage eventMessage) {
//        eventStreamService.pushEvent(eventMessage);
//    }

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 获取动态生成的队列名
        log.info("初始化RabbitMQ监听器，队列名: {}", EventStreamAMQPConfig.EVENT_STREAM_QUEUE);

        // 创建监听容器
        DirectMessageListenerContainer container = new DirectMessageListenerContainer();
        container.setConnectionFactory(rabbitTemplate.getConnectionFactory());
        container.setQueueNames(EventStreamAMQPConfig.EVENT_STREAM_QUEUE);

        // 创建监听适配器
        MessageListenerAdapter adapter = new MessageListenerAdapter(new Object() {
            @SuppressWarnings("unused")
            public void handleMessage(EventMessage message) {
                eventStreamService.pushEvent(message);
            }
        });
        adapter.setMessageConverter(new Jackson2JsonMessageConverter());
        container.setMessageListener(adapter);

        // 启动监听器
        container.start();
    }
}