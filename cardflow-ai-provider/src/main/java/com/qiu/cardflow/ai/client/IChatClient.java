package com.qiu.cardflow.ai.client;

import org.springframework.ai.chat.client.ChatClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 抽象AI聊天客户端
 */
public interface IChatClient {

    String getName();

    ChatClient getChatClient();

    /**
     * 获取模型名称列表
     *
     * @return
     */
    List<String> getModelVONameList();

    default Map<String, ChatClient> getModelVONameToChatClientMap() {
        return getModelVONameList().stream()
                .collect(Collectors.toMap(modelVOName -> modelVOName, modelVOName -> getChatClient()));

    }
}
