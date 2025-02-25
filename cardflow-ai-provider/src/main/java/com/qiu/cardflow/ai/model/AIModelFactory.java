package com.qiu.cardflow.ai.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.qiu.cardflow.ai.supplier.AISupplierProperties;
import com.qiu.cardflow.ai.supplier.AISupplierSimpleFactory;
import com.qiu.cardflow.common.interfaces.exception.Assert;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
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

    public AIModelInstance getAIModelInstance(String modelName) {
        AIModel aiModel = aIModelMap.get(modelName);
        Assert.notNull(aiModel, "模型不存在");

        return aiModel.getInstance();
    }

    public static Set<String> getAIModelNames() {
        return aIModelMap.keySet();
    }

    public static Integer getModelInitialQuota(String modelName) {
        Integer initialQuota = modelInitialQuota.get(modelName);
        Assert.notNull(initialQuota, "该模型的初始额度为空");
        return initialQuota;
    }
}
