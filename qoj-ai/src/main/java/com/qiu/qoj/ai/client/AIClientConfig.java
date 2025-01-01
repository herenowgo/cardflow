package com.qiu.qoj.ai.client;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import org.springframework.ai.zhipuai.ZhiPuAiChatOptions;
import org.springframework.ai.zhipuai.api.ZhiPuAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIClientConfig {

    @Value("${spring.ai.zhipuai.api-key}")
    private String zhiPuAiApiToken;

    @Bean
    ChatClient glm_4() {
        ZhiPuAiApi zhiPuAiApi = new ZhiPuAiApi(zhiPuAiApiToken);

        ChatModel chatModel = new ZhiPuAiChatModel(zhiPuAiApi, ZhiPuAiChatOptions.builder()
                .model(ZhiPuAiApi.ChatModel.GLM_4_Flash.getValue())
                .build());

        return ChatClient.builder(chatModel).build();
    }
}
