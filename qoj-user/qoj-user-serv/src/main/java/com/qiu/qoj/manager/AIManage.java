package com.qiu.qoj.manager;

public interface AIManage {
    String chatForSpeech(String message, String requestId);

    Object chatForDataAnalysis(String message, String requestId);

    String chatWithKnowledgeBase(String message, String requestId, String knowledgeBaseId);
}
