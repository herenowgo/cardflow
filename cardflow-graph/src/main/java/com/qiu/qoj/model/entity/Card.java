package com.qiu.cardflow.model.entity;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.ArrayList;
import java.util.List;

@Data
@Node("Card")
public class Card {
    @Id
    private String id;  // 业务id

    private String question;

    @Relationship(type = "BASED_ON_MATERIAL", direction = Relationship.Direction.OUTGOING)
    private Material basedOnMaterial;

    @Relationship(type = "BASED_ON_CARD", direction = Relationship.Direction.OUTGOING)
    private Card basedOnCard;

    // 添加与知识点的多对多关系
    @Relationship(type = "HAS_TAG", direction = Relationship.Direction.OUTGOING)
    private List<KnowledgeTag> knowledgeTags = new ArrayList<>();

    // 构造函数
    public Card(String id, String question) {
        this.id = id;
        this.question = question;
    }
} 