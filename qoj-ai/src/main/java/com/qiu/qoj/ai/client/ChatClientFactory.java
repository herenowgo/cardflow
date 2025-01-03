package com.qiu.qoj.ai.client;

import java.util.HashMap;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import com.qiu.qoj.ai.model.enums.AIModel;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

@Component
public class ChatClientFactory {
    @Resource
    ChatClient zhiPu;

    @Resource
    ChatClient gemini;

    Map<AIModel, ChatClient> map = new HashMap<>();

    @PostConstruct
    public void init() {
        map.put(AIModel.GLM_4_Flash, zhiPu);
        map.put(AIModel.GLM_4_Air, zhiPu);
        map.put(AIModel.GLM_4_AirX, zhiPu);
        map.put(AIModel.GLM_4_PLUS, zhiPu);
        map.put(AIModel.GEMINI_1_5_PRO_EXP, gemini);
        map.put(AIModel.GEMINI_2_0_FLASH_EXP, gemini);
    }

    public ChatClient getClient(AIModel model) {
        return map.get(model);
    }
}
