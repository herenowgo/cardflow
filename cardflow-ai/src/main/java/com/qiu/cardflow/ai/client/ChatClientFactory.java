package com.qiu.cardflow.ai.client;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

@Component
public class ChatClientFactory {
    @Resource
    private List<IChatClient> chatClientList;

    private HashMap<String, ChatClient> chatClientMap = new HashMap<>();


    @PostConstruct
    public void init() {
        chatClientList.forEach(
                iChatClient -> {
                    chatClientMap.putAll(iChatClient.getModelNameToChatClientMap());
                }
        );
    }

    public ChatClient getChatClient(String modelName) {
        return chatClientMap.get(modelName);
    }
}
