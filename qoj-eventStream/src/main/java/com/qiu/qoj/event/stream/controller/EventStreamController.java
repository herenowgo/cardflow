package com.qiu.qoj.event.stream.controller;

import com.qiu.qoj.event.stream.model.EventMessage;
import com.qiu.qoj.event.stream.service.EventStreamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/stream")
@RequiredArgsConstructor
public class EventStreamController {

    private final EventStreamService eventStreamService;

    @GetMapping(value = "/subscribe/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<EventMessage> subscribe(@PathVariable String userId) {
        return eventStreamService.subscribe(userId);
    }
} 