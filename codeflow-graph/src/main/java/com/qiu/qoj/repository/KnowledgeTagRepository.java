package com.qiu.qoj.repository;

import com.qiu.qoj.model.entity.KnowledgeTag;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgeTagRepository extends Neo4jRepository<KnowledgeTag, String> {
    List<KnowledgeTag> findByName(String name);
} 