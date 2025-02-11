package com.qiu.cardflow.event.stream.config;

import com.qiu.cardflow.event.stream.model.EventMessage;
import com.qiu.cardflow.event.stream.service.EventStreamService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
@RequiredArgsConstructor
public class StreamConfig {

    private final EventStreamService eventStreamService;

    @Bean("eventMessage")
    public Consumer<EventMessage> processEventMessage() {
        return eventStreamService::pushEvent;
    }
} 