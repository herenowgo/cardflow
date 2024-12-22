package com.qiu.qoj.model.entity;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.ArrayList;
import java.util.List;

@Data
@Node("StructuredTag")
public class StructuredTag {
    @Id
    private String name;  // 使用name作为id

    @Relationship(type = "ORGANIZES_CARD", direction = Relationship.Direction.OUTGOING)
    private List<Card> organizedCards = new ArrayList<>();

    @Relationship(type = "ORGANIZES_KNOWLEDGE_TAG", direction = Relationship.Direction.OUTGOING)
    private List<KnowledgeTag> organizedKnowledgeTags = new ArrayList<>();

    // 构造函数
    public StructuredTag(String name) {
        this.name = name;
    }

} 