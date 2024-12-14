package com.qiu.qoj.event.stream.service;

import com.qiu.qoj.event.stream.model.EventMessage;
import com.qiu.qoj.event.stream.model.EventMessageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class EventStreamService {
    private final Map<String, Sinks.Many<EventMessageVO>> userSinks = new ConcurrentHashMap<>();

    public Flux<EventMessageVO> subscribe(String userId) {
        Sinks.Many<EventMessageVO> sink = userSinks.computeIfAbsent(userId,
                k -> Sinks.many().multicast().onBackpressureBuffer());

        return sink.asFlux()
                .doFinally(signalType -> {
                    log.info("User {} connection closed", userId);
                    userSinks.remove(userId);
                });
    }

    public void pushEvent(EventMessage message) {
        String userId = message.getUserId();
        Sinks.Many<EventMessageVO> sink = userSinks.get(userId);

        if (sink != null) {
            sink.tryEmitNext(EventMessage.parseToVO(message));
        }
    }
} 