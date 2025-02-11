package com.qiu.cardflow.repository;

import com.qiu.cardflow.model.entity.StructuredTag;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StructuredTagRepository extends Neo4jRepository<StructuredTag, String> {
    // name就是id，所以不需要额外的查询方法
} 