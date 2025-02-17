package com.qiu.cardflow.ai.client;

import org.springframework.ai.chat.client.ChatClient;

import java.util.List;
import java.util.Map;

/**
 * 抽象AI聊天客户端
 */
public interface IChatClient {

    String getName();

    ChatClient getChatClient();

    /**
     * 获取模型名称列表
     * @return
     */
    List<String> getModelNameList();

    Map<String, ChatClient> getModelNameToChatClientMap();
}
