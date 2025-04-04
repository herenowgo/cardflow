package com.qiu.cardflow.event.stream.service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 心跳服务，定期向所有SSE连接发送心跳消息
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HeartbeatService {

    private final EventStreamService eventStreamService;

    /**
     * 每300秒向所有活跃连接发送一次心跳消息
     */
    @Scheduled(fixedRate = 300000)
    public void sendHeartbeat() {
        log.debug("Sending heartbeat to all active connections");

        Map<String, Object> heartbeatData = new HashMap<>();
        heartbeatData.put("timestamp", Instant.now().toEpochMilli());
        heartbeatData.put("status", "alive");

        eventStreamService.broadcastHeartbeat(heartbeatData);
    }
}
