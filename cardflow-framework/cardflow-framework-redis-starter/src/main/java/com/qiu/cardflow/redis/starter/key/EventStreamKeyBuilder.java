package com.qiu.cardflow.redis.starter.key;

public class EventStreamKeyBuilder extends RedisKeyBuilder {

    public String buildUserToEventStreamKey(String userId) {
        return String.format("eventStream:user:%s", userId);
    }
}
