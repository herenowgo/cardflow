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
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class GoogleChatClient implements IChatClient {

    @Value("${spring.ai.gemini.api-key}")
    private String geminiAiApiToken;

    @Value("${spring.ai.gemini.api-url}")
    private String geminiAiApiUrl;

    /**
     * 会有动态代理，不会重复创建实例
     *
     * @return
     */
    @Bean("googleChatClient1")
    public ChatClient chatClient() {
        OpenAiApi openAiApi = new OpenAiApi(geminiAiApiUrl, geminiAiApiToken);
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
    public List<String> getModelNameList() {
        return Arrays.stream(AIModel.values())
                .map(AIModel::getName)
                .toList();
    }

    @Override
    public Map<String, ChatClient> getModelNameToChatClientMap() {
        return getModelNameList().stream()
                .collect(Collectors.toMap(modelName -> getName() + ":" + modelName, name -> getChatClient()));
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
