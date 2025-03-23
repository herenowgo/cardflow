package com.qiu.cardflow.event.stream.controller;


import com.qiu.cardflow.event.stream.service.EventStreamService;
import com.qiu.codeflow.eventStream.dto.EventMessageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequestMapping("/api/stream")
@RequiredArgsConstructor
public class EventStreamController {

    private final EventStreamService eventStreamService;

    @GetMapping(value = "/subscribe/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<EventMessageVO> subscribe(@PathVariable String userId) {
        log.info("Received SSE connection request for user: {}", userId);

        return eventStreamService.subscribe(userId);
    }
}