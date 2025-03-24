package com.qiu.cardflow.ai.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Data
@AllArgsConstructor
@Slf4j
public class AIModel {
    private String name;
    private List<AIModelInstance> instances;

    public static final long DEFAULT_RECOVERY_TIMEOUT_MILLIS = 30000; // 30秒
    // 熔断配置
    private static final int DEFAULT_FAILURE_THRESHOLD = 3;
    // 实例跟踪器映射表
    private final Map<AIModelInstance, ModelInstanceTracker> trackers = new ConcurrentHashMap<>();
    // 轮询索引
    private final AtomicInteger roundRobinIndex = new AtomicInteger(0);

    public AIModelInstance getInstance() {
        if (trackers.isEmpty()) {
            for (AIModelInstance instance : instances) {
                trackers.put(instance, new ModelInstanceTracker(
                        instance,
                        DEFAULT_FAILURE_THRESHOLD,
                        DEFAULT_RECOVERY_TIMEOUT_MILLIS
                ));
            }
        }
        if (instances == null || instances.isEmpty()) {
            throw new RuntimeException("no AIModelInstance found from AIModel " + name);
        }


        int size = instances.size();
        // 遍历所有实例，找到可用的
        for (int i = 0; i < size; i++) {
            // 轮询选择下一个实例
            int index = Math.abs(roundRobinIndex.getAndIncrement() % size);
            if (roundRobinIndex.get() > 10000) {
                // 防止过大溢出，重置计数器
                roundRobinIndex.set(0);
            }

            AIModelInstance instance = instances.get(index);
            ModelInstanceTracker tracker = trackers.get(instance);

            if (tracker.isAvailable()) {
                log.debug("选择模型实例: {}", instance);
                return instance;
            }
        }

        // 如果所有实例都不可用，尝试使用一个半开状态的实例
        for (AIModelInstance instance : instances) {
            ModelInstanceTracker tracker = trackers.get(instance);
            if (tracker.shouldAttemptReset()) {
                log.info("所有实例不可用，使用半开状态实例: {}", instance);
                return instance;
            }
        }

        // 如果实在没有可用实例，使用第一个实例（降级策略）
        log.warn("所有模型实例都处于熔断状态，降级使用第一个实例: {}", instances.get(0));
        return instances.get(0);
    }

    /**
     * 记录模型实例调用成功
     */
    public void recordSuccess(AIModelInstance instance) {
        ModelInstanceTracker tracker = trackers.get(instance);
        if (tracker != null) {
            tracker.recordSuccess();
        }
    }

    /**
     * 记录模型实例调用失败
     */
    public void recordFailure(AIModelInstance instance) {
        ModelInstanceTracker tracker = trackers.get(instance);
        if (tracker != null) {
            tracker.recordFailure();
        }
    }
}
