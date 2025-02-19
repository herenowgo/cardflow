package com.qiu.cardflow.ai.supplier;

import com.qiu.cardflow.ai.model.ApiProtocol;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "ai")
@Data
public class AISupplierProperties {
    // 供应商配置列表
    private List<ProviderConfig> providers;

    @Data
    public static class ProviderConfig {
        // 供应商名称
        private String name;
        // 供应商基础URL
        private String baseUrl;
        // 供应商API密钥
        private String apiKey;
        // 供应商API协议
        private ApiProtocol apiProtocol;
        // 供应商模型配置列表
        private List<ModelConfig> models;
    }

    @Data
    public static class ModelConfig {
        private String modelNameInSupplier; // 供应商内部模型名称
        private String standardModelName; // 对外暴露的统一模型名称
    }
}
