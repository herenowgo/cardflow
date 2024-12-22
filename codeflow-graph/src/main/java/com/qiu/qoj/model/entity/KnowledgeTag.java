package com.qiu.qoj.model.entity;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

import java.util.ArrayList;
import java.util.List;

@Data
@Node("KnowledgeTag")
public class KnowledgeTag {
    @Id
    @GeneratedValue(UUIDStringGenerator.class)
    private String id;  // 生成的UUID

    private String name;

    @Relationship(type = "LINKED_TO", direction = Relationship.Direction.INCOMING)
    private List<Card> linkedCards = new ArrayList<>();

    // 构造函数
    public KnowledgeTag(String name) {
        this.name = name;
    }

    // Getters and setters
} 