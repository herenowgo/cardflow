package com.qiu.qoj.ai.client;

import com.qiu.qoj.ai.model.enums.AIModel;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class AIClientFactory {
    @Resource
    ChatClient glm_4;

    Map<AIModel, ChatClient> map = new HashMap<>();

    @PostConstruct
    public void init() {
        map.put(AIModel.GLM_4_Flash, glm_4);
        map.put(AIModel.GLM_4_Air, glm_4);
        map.put(AIModel.GLM_4_AirX, glm_4);
        map.put(AIModel.GLM_4_PLUS, glm_4);
    }

    public ChatClient getClient(AIModel model) {
        return map.get(model);
    }
}
