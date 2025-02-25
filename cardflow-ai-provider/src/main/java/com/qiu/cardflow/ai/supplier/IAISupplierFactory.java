package com.qiu.cardflow.ai.supplier;

import com.qiu.cardflow.ai.model.AIModelInstance;
import org.springframework.ai.chat.client.ChatClient;

import java.util.List;

public interface IAISupplierFactory {


    default AISupplier getSupplier(AISupplierProperties.ProviderConfig providerConfig) {
        ChatClient chatClient = getChatClient(providerConfig);
        List<AIModelInstance> modelInstances = getModelInstances(chatClient, providerConfig.getModels());

        return AISupplier.builder()
                .name(providerConfig.getName())
                .chatClient(chatClient)
                .modelInstances(modelInstances)
                .build();
    }

    ChatClient getChatClient(AISupplierProperties.ProviderConfig providerConfig);

    default List<AIModelInstance> getModelInstances(ChatClient chatClient, List<AISupplierProperties.ModelConfig> models) {
        return models.stream()
                .map(model -> {
                    return AIModelInstance.builder()
                            .modelNameInSupplier(model.getModelNameInSupplier())
                            .standardModelName(model.getStandardModelName())
                            .chatClient(chatClient)
                            .initialQuota(model.getInitialQuota() != null ? model.getInitialQuota() : 0)
                            .build();
                })
                .toList();
    }

}
