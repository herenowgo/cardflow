package com.qiu.cardflow.ai.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.ai.chat.client.ChatClient;

@Data
@Builder
public class AIModelInstance {
    // 供应商提供的模型名称
    private String modelNameInSupplier;
    // 模型标准名称
    private String standardModelName;
    // 聊天客户端
    private ChatClient chatClient;

    // 模型实例的初始免费配额
    private Integer initialQuota = 0;
}
