package com.qiu.cardflow.ai.util;

import cn.hutool.core.util.StrUtil;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.ChatOptions;

public class ChatClientRequestSpecBuilder {
    // 聊天选项
    private ChatOptions options;
    // 系统提示
    private String systemPrompt;
    // 用户提示
    private String userPrompt;
    // 聊天记忆
    private ChatMemory chatMemory;
    // 默认对话ID
    private String conversationId;
    // 聊天历史窗口大小
    private Integer chatHistoryWindowSize;

    // 静态构造方法
    public static ChatClientRequestSpecBuilder builder() {
        return new ChatClientRequestSpecBuilder();
    }

    // 设置聊天选项
    public ChatClientRequestSpecBuilder withOptions(ChatOptions options) {
        this.options = options;
        return this;
    }

    // 设置系统提示
    public ChatClientRequestSpecBuilder withSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
        return this;
    }

    // 设置用户提示
    public ChatClientRequestSpecBuilder withUserPrompt(String userPrompt) {
        this.userPrompt = userPrompt;
        return this;
    }

    // 设置聊天记忆
    public ChatClientRequestSpecBuilder withChatMemory(ChatMemory chatMemory) {
        this.chatMemory = chatMemory;
        return this;
    }

    // 设置默认对话ID
    public ChatClientRequestSpecBuilder withConversationId(String conversationId) {
        this.conversationId = conversationId;
        return this;
    }

    // 设置聊天历史窗口大小
    public ChatClientRequestSpecBuilder withChatHistoryWindowSize(Integer chatHistoryWindowSize) {
        this.chatHistoryWindowSize = chatHistoryWindowSize;
        return this;
    }

    // 构建聊天客户端请求规范
    public ChatClient.ChatClientRequestSpec build(ChatClient chatClient) {
        ChatClient.ChatClientRequestSpec spec = chatClient.prompt();
        if (options != null) {
            spec.options(options);
        }

        if (chatMemory != null && conversationId != null && chatHistoryWindowSize != null) {
            MessageChatMemoryAdvisor memoryAdvisor = new MessageChatMemoryAdvisor(
                    chatMemory,
                    conversationId,
                    chatHistoryWindowSize);
            spec.advisors(memoryAdvisor);
        }
        if (StrUtil.isNotEmpty(systemPrompt)) {
            spec.system(systemPrompt);
        }
        if (StrUtil.isNotEmpty(userPrompt)) {
            spec.user(userPrompt);
        }
        return spec;
    }
}
