package com.qiu.qoj.manager.zhipuai;

import com.qiu.qoj.manager.AIManage;
import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.Constants;
import com.zhipu.oapi.service.v4.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
@RequiredArgsConstructor
public class ZhiPuAIServiceImpl implements AIManage {

    private final ClientV4 client;


    @Override
    public String chatForSpeech(String message, String requestId) {
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage chatMessage = new ChatMessage(ChatMessageRole.USER.value(), message);
        messages.add(chatMessage);

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model("codegeex-4")
                .stream(Boolean.FALSE)
                .invokeMethod(Constants.invokeMethod)
                .messages(messages)
                .requestId(requestId)
                .build();
        ModelApiResponse invokeModelApiResp = client.invokeModelApi(chatCompletionRequest);
        return invokeModelApiResp.getData().getChoices().get(0).getMessage().getContent().toString();
    }

    @Override
    public Object chatForDataAnalysis(String message, String requestId) {
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage chatMessage = new ChatMessage(ChatMessageRole.USER.value(), message);
        ChatMessage assistantMessage = new ChatMessage(ChatMessageRole.ASSISTANT.value(), "65a265419d72d299a9230616");
        messages.add(chatMessage);
        messages.add(assistantMessage);

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model("glm-4-assistant")
                .stream(Boolean.FALSE)
                .invokeMethod(Constants.invokeMethod)
                .messages(messages)
                .requestId(requestId)
                .build();
        ModelApiResponse invokeModelApiResp = client.invokeModelApi(chatCompletionRequest);
        return invokeModelApiResp.getData().getChoices().get(0).getMessage().getContent().toString();
    }

    @Override
    public String chatWithKnowledgeBase(String message, String requestId, String knowledgeBaseId) {
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage chatMessage = new ChatMessage(ChatMessageRole.USER.value(), message);
        messages.add(chatMessage);

        ArrayList<ChatTool> chatTools = new ArrayList<>();
        ChatTool chatTool = new ChatTool();
        chatTool.setType("retrieval");
        Retrieval retrieval = new Retrieval();
        retrieval.setKnowledge_id(knowledgeBaseId);
        retrieval.setPrompt_template("从文档\\n\\\"\\\"\\\"\\n{{knowledge}}\\n\\\"\\\"\\\"\\n中(文档中的tags列是题目类型数组)找能满足\\n\\\"\\\"\\\"\\n{{question}}\\n\\\"\\\"\\\"\\n的题目，找到合适的就返回题目的title,以及一些学习建议，并在最后集中返回这些题目的id（开始集中返回的格式为'id::::',然后每个id之间有逗号隔开,但是最后直接开始输出“id::::”,不要说'相关的id集中返回格式如下'等提示）\\n不要复述问题，直接开始回答。");
        chatTool.setRetrieval(retrieval);
        chatTools.add(chatTool);

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model("glm-4")
                .stream(Boolean.FALSE)
                .invokeMethod(Constants.invokeMethod)
                .messages(messages)
                .tools(chatTools)
                .requestId(requestId)
                .build();
        ModelApiResponse invokeModelApiResp = client.invokeModelApi(chatCompletionRequest);
//        System.out.println("model output:" + invokeModelApiResp);
        return invokeModelApiResp.getData().getChoices().get(0).getMessage().getContent().toString();
    }
}
