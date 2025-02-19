package com.qiu.cardflow.ai.supplier;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import org.springframework.ai.zhipuai.ZhiPuAiChatOptions;
import org.springframework.ai.zhipuai.api.ZhiPuAiApi;
import org.springframework.stereotype.Component;

@Component
public class AISupplierFactoryOfZhiPu implements IAISupplierFactory {


    @Override
    public ChatClient getChatClient(AISupplierProperties.ProviderConfig providerConfig) {
        ZhiPuAiApi zhiPuAiApi = new ZhiPuAiApi(providerConfig.getApiKey());

        ChatModel chatModel = new ZhiPuAiChatModel(zhiPuAiApi, ZhiPuAiChatOptions.builder()
                .model(ZhiPuAiApi.ChatModel.GLM_4_Flash.getValue())
                .build());

        return ChatClient.builder(chatModel).build();
    }
}
