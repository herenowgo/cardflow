package com.qiu.cardflow.ai.client;

import lombok.Getter;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import org.springframework.ai.zhipuai.ZhiPuAiChatOptions;
import org.springframework.ai.zhipuai.api.ZhiPuAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class ZhiPuChatClient implements IChatClient {

    @Value("${spring.ai.zhipuai.api-key}")
    private String zhiPuAiApiToken;

    /**
     * 会有动态代理，不会重复创建实例
     *
     * @return
     */
    @Bean("zhiPuChatClient1")
    public ChatClient chatClient() {
        ZhiPuAiApi zhiPuAiApi = new ZhiPuAiApi(zhiPuAiApiToken);

        ChatModel chatModel = new ZhiPuAiChatModel(zhiPuAiApi, ZhiPuAiChatOptions.builder()
                .model(ZhiPuAiApi.ChatModel.GLM_4_Flash.getValue())
                .build());

        return ChatClient.builder(chatModel).build();
    }


    @Override
    public String getName() {
        return "zhipu";
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
        GLM_4_Flash(ZhiPuAiApi.ChatModel.GLM_4_Flash.getValue()),
        ;

        final String name;

        AIModel(String name) {
            this.name = name;
        }
    }
}
