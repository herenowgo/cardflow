package com.qiu.cardflow.ai.manager.zhipuai;

import com.qiu.cardflow.ai.manager.AIManage;
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
    public String chatForDataAnalysis(String message, String requestId) {

        message = "# 提示词：算法题目提交记录数据分析\n\n"
                + "## 上下文\n"
                + "- **数据类型**：算法题目的提交记录。\n"
                + "- **目标字段**：编程语言、判题信息、判题状态、题目ID、创建时间、标签。\n\n"
                + "## 目标\n"
                + "- **分析目的**：为用户提供算法学习建议。\n"
                + "- **关键分析**：用户常使用的编程语言、常见的判题错误类型、用户在不同难度题目上的表现、用户在不同知识点上的表现。\n\n"
                + "## 风格\n"
                + "- **专业术语**：使用数据分析和算法领域的专业术语。\n"
                + "- **简洁明了**：确保提示词简洁、易于理解。\n\n"
                + "## 语气\n"
                + "- **客观分析**：以客观、事实为基础的分析方式。\n"
                + "- **鼓励性**：提供积极的反馈和建议，鼓励用户改进。\n\n"
                + "## 受众\n"
                + "- **用户群体**：对算法学习有兴趣的用户，可能包括初学者和进阶者。\n\n"
                + "## 响应\n"
                + "- **输出格式**：分析报告，包括图表和文字描述。\n"
                + "- **内容要求**：包含关键指标分析、趋势观察、具体建议。\n\n"
                + "## 工作流程\n"
                + "1. **数据预处理**：清洗和格式化数据，确保数据质量。\n"
                + "2. **数据分析**：分析编程语言使用情况、判题状态分布、题目难度和知识点标签。\n"
                + "3. **趋势观察**：识别用户在算法学习中的趋势，如进步或停滞。\n"
                + "4. **建议生成**：基于分析结果，为用户提供个性化的学习建议。\n\n"
                + "## 示例\n"
                + "- **输入**：用户的算法题目提交记录数据。\n"
                + "- **输出**：一个分析报告，包括用户最常用的编程语言、最常见的判题错误、在特定难度和知识点上的表现，以及针对用户的学习建议。"
                + "用户的题目提交记录的csv字符串为：\n" + message;
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
