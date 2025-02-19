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
public class OpenRouterChatClient implements IChatClient {

    @Value("${spring.ai.openrouter.api-key}")
    private String aiApiToken;

    @Value("${spring.ai.openrouter.api-url}")
    private String aiApiUrl;

    /**
     * 会有动态代理，不会重复创建实例
     *
     * @return
     */
    @Bean("openRouterChatClient1")
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
        return "openrouter";
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
        GEMINI_2_0_PRC_EXP_02_05_FREE("google/gemini-2.0-pro-exp-02-05:free"),
        DEEPSEEK_R1("deepseek/deepseek-r1:free"),
        DEEPSEEK_V3("deepseek/deepseek-chat:free")
        ;

        private final String name;

        AIModel(String name) {
            this.name = name;
        }
    }
}
