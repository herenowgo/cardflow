package com.qiu.cardflow.graph.model.entity;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Data
@Node("Tag")
public class TagNode {
    @Id
    private String name;
} 