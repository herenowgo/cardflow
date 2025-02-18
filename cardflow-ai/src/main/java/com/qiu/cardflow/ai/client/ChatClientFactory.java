package com.qiu.cardflow.ai.client;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ChatClientFactory {

    @Resource
    private List<IChatClient> chatClientList;

    private HashMap<String, ChatClient> chatClientMap = new HashMap<>();
    private HashMap<String, String> ModelVONameToModelNameMap = new HashMap<>();

    @PostConstruct
    public void init() {
        chatClientList.forEach(
                iChatClient -> {
                    chatClientMap.putAll(iChatClient.getModelNameToChatClientMap());
                    ModelVONameToModelNameMap.putAll(iChatClient.getModelNameList().stream().collect(Collectors.toMap(name-> name, name -> name.substring(name.indexOf(":") + 1))));
                }
        );
    }

    public ChatClient getChatClient(String modelName) {
        return chatClientMap.get(modelName);
    }
    public String getRealModelName(String modelVOName) {
        return ModelVONameToModelNameMap.get(modelVOName);
    }
}
