package com.qiu.qoj.model.entity;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

@RelationshipProperties
public class KnowledgeTagRelation {
    @Id
    @GeneratedValue
    private Long id;

    @TargetNode
    private KnowledgeTag target;

    private RelationType type;  // 关系类型
    private Double weight;      // 关系权重（0-1）
    private String description; // 关系描述
    private Integer cooccurrenceCount; // 共现次数

    public enum RelationType {
        SIMILAR("相似"),
        PREREQUISITE("前置"),
        EXTENSION("扩展"),
        OPPOSITE("相反"),
        RELATED("相关");

        private final String description;

        RelationType(String description) {
            this.description = description;
        }
    }
} 