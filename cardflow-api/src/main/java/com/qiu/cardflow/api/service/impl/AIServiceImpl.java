package com.qiu.cardflow.api.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.qiu.cardflow.ai.dto.ChatRequestDTO;
import com.qiu.cardflow.ai.dto.StructuredOutputRequestDTO;
import com.qiu.cardflow.ai.interfaces.IAIRPC;
import com.qiu.cardflow.api.service.IAIService;
import com.qiu.cardflow.api.vo.ai.ChatRequest;
import com.qiu.cardflow.web.starter.context.UserContext;
import com.qiu.codeflow.eventStream.dto.EventType;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

@Service
public class AIServiceImpl implements IAIService {

    @DubboReference
    private IAIRPC aiRpc;


    @Override
    public String chat(ChatRequest chatRequest) {
        ChatRequestDTO chatRequestDTO = ChatRequestDTO.builder()
                .maxSize(10)
                .maxMills(400)
                .chatHistoryWindowSize(6)
                .eventType(EventType.ANSWER)
                .userId(UserContext.getUserId().toString())
                .build();
        BeanUtil.copyProperties(chatRequest, chatRequestDTO);
        return aiRpc.chat(chatRequestDTO);
    }

    @Override
    public String structuredOutput(StructuredOutputRequestDTO structuredOutputRequestDTO) {
        return aiRpc.structuredOutput(structuredOutputRequestDTO);
    }

    @Override
    public String generateCards(ChatRequest chatRequest) {
        return null;
    }
}
