package com.qiu.qoj.model.entity;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

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

    // 构造函数
    public Card(String id, String question) {
        this.id = id;
        this.question = question;
    }
} 