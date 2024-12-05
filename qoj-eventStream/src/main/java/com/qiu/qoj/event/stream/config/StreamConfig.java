package com.qiu.qoj.event.stream.config;

import com.qiu.qoj.event.stream.model.EventMessage;
import com.qiu.qoj.event.stream.service.EventStreamService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
@RequiredArgsConstructor
public class StreamConfig {

    private final EventStreamService eventStreamService;

    @Bean("judgeResult")
    public Consumer<EventMessage> processJudgeResult() {
        return eventStreamService::pushEvent;
    }
} 