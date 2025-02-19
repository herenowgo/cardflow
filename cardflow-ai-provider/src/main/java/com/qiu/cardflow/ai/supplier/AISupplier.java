package com.qiu.cardflow.ai.supplier;

import com.qiu.cardflow.ai.model.AIModelInstance;
import lombok.Builder;
import lombok.Data;
import org.springframework.ai.chat.client.ChatClient;

import java.util.List;

/**
 * AI供应商
 */
@Data
@Builder
public class AISupplier {
    // AI提供者的名称
    private String name;
    // 聊天客户端
    private ChatClient chatClient;
    // AI模型实例列表
    private List<AIModelInstance> modelInstances;
}
