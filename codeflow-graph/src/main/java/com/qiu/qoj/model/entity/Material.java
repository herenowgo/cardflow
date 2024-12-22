package com.qiu.qoj.model.entity;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.ArrayList;
import java.util.List;

@Data
@Node("Material")
public class Material {
    @Id
    private String id;  // 业务id

    private String name;

    @Relationship(type = "STRUCTURED_BY", direction = Relationship.Direction.OUTGOING)
    private List<StructuredTag> structuredTags = new ArrayList<>();

    @Relationship(type = "COVERS", direction = Relationship.Direction.OUTGOING)
    private List<KnowledgeTag> coveredKnowledge = new ArrayList<>();

    // 构造函数
    public Material(String id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getters and setters
} 