package com.qiu.cardflow.model.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CardGraphDTO {
    private String id;          // 卡片业务ID
    private String question;    // 问题
    private String materialId;  // 学习资料ID
    private List<String> knowledgeTagNames; // 知识点名称列表
    private String structuredTagPath;  // 结构化标签路径（例如：第一章;;第一节）
} 