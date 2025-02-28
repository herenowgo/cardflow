package com.qiu.cardflow.event.stream.service;

import com.qiu.cardflow.event.stream.model.EventMessage;
import com.qiu.cardflow.event.stream.model.EventMessageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
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
}