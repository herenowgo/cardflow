package com.qiu.cardflow.model.entity;

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

    // 添加知识点之间的关系
    @Relationship(type = "RELATED_TO", direction = Relationship.Direction.OUTGOING)
    private List<KnowledgeTagRelation> relatedTags = new ArrayList<>();

    // 添加父子关系，用于表示知识体系的层级结构
    @Relationship(type = "IS_CHILD_OF", direction = Relationship.Direction.OUTGOING)
    private KnowledgeTag parent;

    @Relationship(type = "IS_PARENT_OF", direction = Relationship.Direction.OUTGOING)
    private List<KnowledgeTag> children = new ArrayList<>();

    // 构造函数
    public KnowledgeTag(String name) {
        this.name = name;
    }

    // Getters and setters
} 