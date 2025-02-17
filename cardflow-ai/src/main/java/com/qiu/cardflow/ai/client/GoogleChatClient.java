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
     * @return
     */
    @Bean
    public ChatClient chatClient() {
        OpenAiApi openAiApi = new OpenAiApi(geminiAiApiUrl, geminiAiApiToken);
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model("gemini-exp-1206")
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
        return Arrays.stream(GoogleAIModel.values())
                .map(GoogleAIModel::getName)
                .toList();
    }

    @Override
    public Map<String, ChatClient> getModelNameToChatClientMap() {
        return getModelNameList().stream()
                .collect(Collectors.toMap(modelName -> getName() + ":" + modelName, name -> getChatClient()));
    }




    @Getter
    public enum GoogleAIModel {
        GEMINI_2_0_FLASH_EXP("gemini-2.0-flash-exp"),
        GEMINI_2_1_FLASH_EXP("gemini-2.1-flash-exp"),
        GEMINI_2_3_FLASH_EXP("gemini-2.3-flash-exp")

        ;

        final String name;

        GoogleAIModel(String name) {
            this.name = name;
        }
    }
}
