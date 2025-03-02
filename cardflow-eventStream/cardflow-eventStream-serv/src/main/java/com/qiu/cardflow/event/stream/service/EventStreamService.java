package com.qiu.cardflow.event.stream.service;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.qiu.cardflow.event.stream.model.EventMessage;
import com.qiu.cardflow.event.stream.model.EventMessageVO;
import com.qiu.cardflow.event.stream.model.EventType;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Slf4j
@Service
public class EventStreamService {
    // 修改为Map<String, Set<SinkInfo>>结构，为每个用户存储多个连接
    private final Map<String, Set<SinkInfo>> userSinks = new ConcurrentHashMap<>();

    // 内部类，封装sink和连接ID
    private static class SinkInfo {
        private final String connectionId;
        private final Sinks.Many<EventMessageVO> sink;

        public SinkInfo(String connectionId, Sinks.Many<EventMessageVO> sink) {
            this.connectionId = connectionId;
            this.sink = sink;
        }

        public String getConnectionId() {
            return connectionId;
        }

        public Sinks.Many<EventMessageVO> getSink() {
            return sink;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null || getClass() != obj.getClass())
                return false;
            SinkInfo other = (SinkInfo) obj;
            return connectionId.equals(other.connectionId);
        }

        @Override
        public int hashCode() {
            return connectionId.hashCode();
        }
    }

    public Flux<EventMessageVO> subscribe(String userId) {
        // 为每个连接生成唯一ID
        String connectionId = UUID.randomUUID().toString();
        Sinks.Many<EventMessageVO> sink = Sinks.many().multicast().onBackpressureBuffer();

        // 确保用户的连接集合存在
        userSinks.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet());

        // 添加新连接到用户的连接集合
        SinkInfo sinkInfo = new SinkInfo(connectionId, sink);
        userSinks.get(userId).add(sinkInfo);

        log.info("User {} new connection established: {}, total connections: {}",
                userId, connectionId, userSinks.get(userId).size());

        // 立即发送连接成功消息
        sendInitialConnectionMessage(sink);

        return sink.asFlux()
                .doFinally(signalType -> {
                    // 连接关闭时，移除对应的sink
                    Set<SinkInfo> userConnections = userSinks.get(userId);
                    if (userConnections != null) {
                        userConnections.removeIf(info -> info.getConnectionId().equals(connectionId));
                        log.info("User {} connection {} closed, remaining connections: {}",
                                userId, connectionId, userConnections.size());

                        // 如果用户没有更多连接，则移除用户条目
                        if (userConnections.isEmpty()) {
                            userSinks.remove(userId);
                            log.info("Removed user {} from sink map as all connections closed", userId);
                        }
                    }
                });
    }

    /**
     * 向新连接发送初始连接成功消息
     * 
     * @param userId       用户ID
     * @param connectionId 连接ID
     * @param sink         Sink对象
     */
    private void sendInitialConnectionMessage(Sinks.Many<EventMessageVO> sink) {
        EventMessage connectMessage = new EventMessage();
        connectMessage.setEventType(EventType.HEARTBEAT); // 使用HEARTBEAT类型或者可以添加新的CONNECTION_ESTABLISHED类型
        connectMessage.setData("init");
        connectMessage.setSequence(0);
        connectMessage.setRequestId("init");

        EventMessageVO connectVO = EventMessage.parseToVO(connectMessage);

        // log.debug("Sending initial connection message to user {} with connection {}",
        // userId, connectionId);
        sink.tryEmitNext(connectVO);
    }

    public void pushEvent(EventMessage message) {
        String userId = message.getUserId();
        Set<SinkInfo> sinkInfoSet = userSinks.get(userId);

        if (sinkInfoSet != null && !sinkInfoSet.isEmpty()) {
            EventMessageVO messageVO = EventMessage.parseToVO(message);
            log.debug("Pushing event to user {}, active connections: {}", userId, sinkInfoSet.size());

            // 向用户的所有连接推送消息
            for (SinkInfo sinkInfo : sinkInfoSet) {
                sinkInfo.getSink().tryEmitNext(messageVO);
            }
        }
    }

    /**
     * 向所有连接的客户端广播心跳消息
     *
     * @param heartbeatData 心跳数据
     */
    public void broadcastHeartbeat(Map<String, Object> heartbeatData) {
        if (userSinks.isEmpty()) {
            log.debug("No active connections for heartbeat");
            return;
        }

        EventMessage heartbeatMessage = new EventMessage();
        heartbeatMessage.setEventType(EventType.HEARTBEAT);
        heartbeatMessage.setData(heartbeatData);
        heartbeatMessage.setSequence(0);
        heartbeatMessage.setRequestId("heartbeat");

        EventMessageVO heartbeatVO = EventMessage.parseToVO(heartbeatMessage);

        // 向所有连接的客户端发送心跳
        int connectionCount = 0;
        for (Map.Entry<String, Set<SinkInfo>> entry : userSinks.entrySet()) {
            for (SinkInfo sinkInfo : entry.getValue()) {
                try {
                    sinkInfo.getSink().tryEmitNext(heartbeatVO);
                    connectionCount++;
                } catch (Exception e) {
                    log.error("Failed to send heartbeat to user {} connection {}",
                            entry.getKey(), sinkInfo.getConnectionId(), e);
                }
            }
        }

        log.debug("Heartbeat sent to {} connections across {} users",
                connectionCount, userSinks.size());
    }
}