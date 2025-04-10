package com.qiu.cardflow.graph.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.data.neo4j.core.schema.Relationship.Direction.INCOMING;
import static org.springframework.data.neo4j.core.schema.Relationship.Direction.OUTGOING;

@Node("Card")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CardNode {
    @Id
    private String cardId;

    @Relationship(type = "CREATED", direction = INCOMING)
    private UserNode userNode;

    @Relationship(type = "HAS_TAG", direction = OUTGOING)
    private List<TagNode> tagNodeList = new ArrayList<>();
} 