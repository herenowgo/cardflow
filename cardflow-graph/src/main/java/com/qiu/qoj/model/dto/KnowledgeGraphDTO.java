package com.qiu.cardflow.model.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class KnowledgeGraphDTO {
    private List<KnowledgeNodeDTO> nodes;
    private List<KnowledgeRelationDTO> relations;

    @Data
    @Builder
    public static class KnowledgeNodeDTO {
        private String id;
        private String name;
        private String type;  // 节点类型：ROOT, BRANCH, LEAF
    }

    @Data
    @Builder
    public static class KnowledgeRelationDTO {
        private String sourceId;
        private String targetId;
        private String type;      // 关系类型
        private Double weight;    // 关系权重
        private Integer cooccurrenceCount;  // 共现次数
    }
} 