package com.qiu.cardflow.graph.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node("User")
@Data
@AllArgsConstructor
public class UserNode {
    @Id
    private Long userId;
} 