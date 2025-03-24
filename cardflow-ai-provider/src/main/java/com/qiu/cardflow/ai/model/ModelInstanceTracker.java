package com.qiu.cardflow.ai.model;

import com.qiu.cardflow.ai.constant.CircuitState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 跟踪AI模型实例的熔断状态
 */
@Data
@Slf4j
public class ModelInstanceTracker {
    private final AIModelInstance instance;
    private final AtomicReference<CircuitState> state = new AtomicReference<>(CircuitState.CLOSED);
    private final AtomicInteger failureCount = new AtomicInteger(0);
    // 配置参数
    private final int failureThreshold;
    private volatile LocalDateTime lastFailureTime;
    private volatile LocalDateTime openTime;
    private volatile long recoveryTimeoutMillis;

    public ModelInstanceTracker(AIModelInstance instance, int failureThreshold, long recoveryTimeoutMillis) {
        this.instance = instance;
        this.failureThreshold = failureThreshold;
        this.recoveryTimeoutMillis = recoveryTimeoutMillis;
    }

    /**
     * 记录调用成功
     */
    public void recordSuccess() {
        if (state.get() == CircuitState.HALF_OPEN) {
            // 半开状态下成功，恢复为关闭状态
            if (state.compareAndSet(CircuitState.HALF_OPEN, CircuitState.CLOSED)) {
                log.info("模型实例 {} 恢复正常", instance);
            }
        }
        // 重置失败计数
        failureCount.set(0);
    }

    /**
     * 记录调用失败
     */
    public void recordFailure() {
        lastFailureTime = LocalDateTime.now();

        CircuitState currentState = state.get();
        if (currentState == CircuitState.HALF_OPEN) {
            // 半开状态下失败，回到熔断状态
            if (state.compareAndSet(CircuitState.HALF_OPEN, CircuitState.OPEN)) {
                openTime = LocalDateTime.now();
                log.warn("模型实例 {} 半开状态下再次失败，重新熔断", instance);
            }
            return;
        }

        if (currentState == CircuitState.CLOSED) {
            // 关闭状态下累计失败次数
            int current = failureCount.incrementAndGet();
            log.debug("模型实例 {} 失败计数: {}/{}", instance, current, failureThreshold);

            if (current >= failureThreshold) {
                // 达到阈值，触发熔断
                if (state.compareAndSet(CircuitState.CLOSED, CircuitState.OPEN)) {
                    openTime = LocalDateTime.now();
                    log.warn("模型实例 {} 达到失败阈值 {}，触发熔断", instance, failureThreshold);
                }
            }
        }
    }

    /**
     * 检查是否应该尝试恢复（进入半开状态）
     */
    public boolean shouldAttemptReset() {
        CircuitState currentState = state.get();

        if (currentState == CircuitState.HALF_OPEN) {
            return true;
        }

        if (currentState == CircuitState.OPEN && openTime != null) {
            long elapsedMillis = java.time.Duration.between(openTime, LocalDateTime.now()).toMillis();

            if (elapsedMillis >= recoveryTimeoutMillis) {
                // 熔断时间已过，尝试切换到半开状态
                if (state.compareAndSet(CircuitState.OPEN, CircuitState.HALF_OPEN)) {
                    // 更新恢复超时时间（使用双倍策略）
                    recoveryTimeoutMillis *= 2;
                    log.info("模型实例 {} 进入半开状态，尝试恢复", instance);
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 检查模型实例是否可用
     */
    public boolean isAvailable() {
        CircuitState currentState = state.get();
        return currentState == CircuitState.CLOSED ||
                (currentState == CircuitState.HALF_OPEN) ||
                shouldAttemptReset();
    }
}