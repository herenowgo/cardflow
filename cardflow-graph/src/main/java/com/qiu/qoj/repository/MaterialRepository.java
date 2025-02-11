package com.qiu.cardflow.repository;

import com.qiu.cardflow.model.entity.Material;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaterialRepository extends Neo4jRepository<Material, String> {
    List<Material> findByName(String name);
} 