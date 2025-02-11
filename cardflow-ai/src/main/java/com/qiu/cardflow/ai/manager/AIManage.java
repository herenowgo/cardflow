package com.qiu.cardflow.ai.manager;

public interface AIManage {
    String chatForSpeech(String message, String requestId);

    String chatForDataAnalysis(String message, String requestId);

    String chatWithKnowledgeBase(String message, String requestId, String knowledgeBaseId);
}
