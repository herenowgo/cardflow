package com.qiu.cardflow.ai.client;

import lombok.Getter;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class VolcanoChatClient implements IChatClient {

    @Value("${spring.ai.volcano.api-key}")
    private String aiApiToken;

    @Value("${spring.ai.volcano.api-url}")
    private String aiApiUrl;

    /**
     * 会有动态代理，不会重复创建实例
     *
     * @return
     */
    @Bean("VolcanoChatClient1")
    public ChatClient chatClient() {
        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl(aiApiUrl)
                .apiKey(aiApiToken)
                .build();
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .build();
        ChatModel chatModel = OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(options)
                .build();

        return ChatClient.builder(chatModel).build();
    }


    @Override
    public String getName() {
        return "volcano";
    }

    @Override
    public ChatClient getChatClient() {
        return chatClient();
    }

    @Override
    public List<String> getModelVONameList() {
        return Arrays.stream(AIModel.values())
                .map(aiModel -> getName() + ":" + aiModel.getName())
                .toList();
    }


    @Getter
    public enum AIModel {
        DEEPSEEK_V3("ep-20250219113402-sqh2k"),

        ;

        private final String name;

        AIModel(String name) {
            this.name = name;
        }
    }
}
