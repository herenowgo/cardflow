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
public class GoogleChatClient implements IChatClient {

    @Value("${spring.ai.gemini.api-key}")
    private String aiApiToken;

    @Value("${spring.ai.gemini.api-url}")
    private String aiApiUrl;

    /**
     * 会有动态代理，不会重复创建实例
     *
     * @return
     */
    @Bean("googleChatClient1")
    public ChatClient chatClient() {
        OpenAiApi openAiApi = new OpenAiApi(aiApiUrl, aiApiToken);
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model("gemini-2.0-flash-exp")
                .build();
        ChatModel chatModel = new OpenAiChatModel(openAiApi, options);
        return ChatClient.builder(chatModel).build();
    }


    @Override
    public String getName() {
        return "gemini";
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
        GEMINI_2_0_FLASH_EXP("gemini-2.0-flash-exp"),
        ;

        final String name;

        AIModel(String name) {
            this.name = name;
        }
    }
}
