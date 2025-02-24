package com.qiu.cardflow.ai.model;

import com.qiu.cardflow.ai.supplier.AISupplierFactory;
import com.qiu.cardflow.ai.supplier.AISupplierProperties;
import com.qiu.cardflow.common.interfaces.exception.Assert;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AIModelFactory {

    private final AISupplierProperties aiSupplierProperties;

    private final AISupplierFactory aiSupplierFactory;

    public static Map<String, AIModel> aIModelMap = new HashMap<>();

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
        });
    }

    public AIModelInstance getAIModelInstance(String modelName) {
        AIModel aiModel = aIModelMap.get(modelName);
        Assert.notNull(aiModel, "模型不存在");

        return aiModel.getInstance();
    }
}
