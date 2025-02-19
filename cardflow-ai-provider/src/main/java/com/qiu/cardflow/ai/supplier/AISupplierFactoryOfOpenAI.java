package com.qiu.cardflow.ai.supplier;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Component;

@Component
public class AISupplierFactoryOfOpenAI implements IAISupplierFactory {


    @Override
    public ChatClient getChatClient(AISupplierProperties.ProviderConfig providerConfig) {
        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl(providerConfig.getBaseUrl())
                .apiKey(providerConfig.getApiKey())
                .build();
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .build();
        ChatModel chatModel = OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(options)
                .build();

        return ChatClient.builder(chatModel).build();
    }
}
