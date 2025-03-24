package com.qiu.cardflow.ai.model;

import com.qiu.cardflow.ai.constant.CircuitState;
import com.qiu.cardflow.ai.supplier.AISupplierProperties;
import com.qiu.cardflow.ai.supplier.AISupplierSimpleFactory;
import com.qiu.cardflow.common.interfaces.exception.Assert;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static com.qiu.cardflow.ai.model.AIModel.DEFAULT_RECOVERY_TIMEOUT_MILLIS;

@Component
@RequiredArgsConstructor
@Slf4j
public class AIModelFactory {

    private final AISupplierProperties aiSupplierProperties;

    private final AISupplierSimpleFactory aiSupplierFactory;

    private static Map<String, AIModel> aIModelMap = new HashMap<>();

    private static Map<String, Integer> modelInitialQuota = new HashMap<>();

    @PostConstruct
    private void init() {
        Map<String, List<AIModelInstance>> aIModelInstanceMap = aiSupplierProperties.getProviders().stream()
                .map(providerConfig -> {
                    return aiSupplierFactory.getAISupplier(providerConfig).getModelInstances();
                })
                .flatMap(Collection::stream)
                .collect(Collectors.groupingBy(AIModelInstance::getStandardModelName));

        aIModelInstanceMap.entrySet().forEach(entry -> {
            aIModelMap.put(entry.getKey(), new AIModel(entry.getKey(), entry.getValue()));
            modelInitialQuota.put(entry.getKey(), entry.getValue().stream().mapToInt(AIModelInstance::getInitialQuota).sum());
        });
    }


    public AIModel getAIModel(String modelName) {
        AIModel aiModel = aIModelMap.get(modelName);
        Assert.notNull(aiModel, "模型不存在");

        return aiModel;
    }

    public static Set<String> getAIModelNames() {
        return aIModelMap.keySet();
    }

    public static Integer getModelInitialQuota(String modelName) {
        Integer initialQuota = modelInitialQuota.get(modelName);
        Assert.notNull(initialQuota, "该模型的初始额度为空");
        return initialQuota;
    }

    /**
     * 每天0点重置所有熔断器参数
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void resetAllCircuitBreakers() {
        log.info("开始重置所有模型实例的熔断参数");

        for (AIModel aiModel : aIModelMap.values()) {
            for (Map.Entry<AIModelInstance, ModelInstanceTracker> entry : aiModel.getTrackers().entrySet()) {
                ModelInstanceTracker tracker = entry.getValue();

                // 使用CAS操作重置熔断参数
                CircuitState currentState = tracker.getState().get();
                if (currentState == CircuitState.OPEN) {
                    // 尝试将状态从OPEN切换到HALF_OPEN
                    if (tracker.getState().compareAndSet(CircuitState.OPEN, CircuitState.HALF_OPEN)) {
                        // 成功切换状态后，重置恢复超时时间
                        tracker.setRecoveryTimeoutMillis(DEFAULT_RECOVERY_TIMEOUT_MILLIS);
                        log.info("模型 {} 的实例 {} 熔断参数已重置",
                                aiModel.getName(), entry.getKey());
                    } else {
                        // CAS失败，说明状态已被其他线程修改
                        log.info("模型 {} 的实例 {} 状态已被其他线程修改，跳过重置",
                                aiModel.getName(), entry.getKey());
                    }
                } else {
                    log.info("模型 {} 的实例 {} 当前状态为 {}，无需重置",
                            aiModel.getName(), entry.getKey(), currentState);
                }
            }
        }
    }
}
