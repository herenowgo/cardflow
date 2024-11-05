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
        retrieval.setPrompt_template("从文档\n" +
                "\"\"\"\n" +
                "{{knowledge}}\n" +
                "\"\"\"\n" +
                "中找问题\n" +
                "\"\"\"\n" +
                "{{question}}\n" +
                "\"\"\"\n" +
                "的答案，找到答案就直接返回那些题目ID，以“ID:::开始”，以“ID:::”结束，不要说其余的话\n" +
                "\n" +
                "不要复述问题，直接开始回答。");
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
