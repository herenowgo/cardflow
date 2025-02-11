package com.qiu.cardflow.model.entity;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.ArrayList;
import java.util.List;

@Data
@Node("User")
public class User {
    @Id
    private Long id;  // 用户id

    @Relationship(type = "SAVED", direction = Relationship.Direction.OUTGOING)
    private List<Material> savedMaterials = new ArrayList<>();

    @Relationship(type = "TAGGED", direction = Relationship.Direction.OUTGOING)
    private List<KnowledgeTag> taggedKnowledge = new ArrayList<>();

    // 构造函数
    public User(Long id) {
        this.id = id;
    }
}