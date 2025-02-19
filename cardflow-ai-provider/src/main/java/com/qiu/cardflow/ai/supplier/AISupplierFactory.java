package com.qiu.cardflow.ai.supplier;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AISupplierFactory {

    private final AISupplierFactoryOfOpenAI aisupplierFactoryOfOpenAI;

    private final AISupplierFactoryOfZhiPu aisupplierFactoryOfZhiPu;

    public AISupplier getAISupplier(AISupplierProperties.ProviderConfig providerConfig) {
         IAISupplierFactory aiSupplierFactory = switch (providerConfig.getApiProtocol()) {
            case OPENAI -> aisupplierFactoryOfOpenAI;
            case ZHI_PU -> aisupplierFactoryOfZhiPu;
            default -> throw new IllegalArgumentException("不支持的api协议: " + providerConfig);
        };
        return aiSupplierFactory.getSupplier(providerConfig);
    }
}
