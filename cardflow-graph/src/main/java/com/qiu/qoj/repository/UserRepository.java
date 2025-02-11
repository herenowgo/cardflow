package com.qiu.cardflow.repository;

import com.qiu.cardflow.model.entity.User;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends Neo4jRepository<User, Long> {
    // 基本的CRUD方法由Neo4jRepository提供
}