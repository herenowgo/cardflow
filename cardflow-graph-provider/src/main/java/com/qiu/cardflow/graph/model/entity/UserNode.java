package com.qiu.cardflow.graph.model.entity;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.List;

import static org.springframework.data.neo4j.core.schema.Relationship.Direction.OUTGOING;

@Node("User")
@Data
public class UserNode {
    @Id
    private Long userId;

    @Relationship(type = "CREATED", direction = OUTGOING)
    private List<CardNode> cardNodes;
} 