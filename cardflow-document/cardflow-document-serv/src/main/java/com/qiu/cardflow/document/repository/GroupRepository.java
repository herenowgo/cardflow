package com.qiu.cardflow.document.repository;

import com.qiu.cardflow.document.model.entity.Group;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GroupRepository extends MongoRepository<Group, String> {
    // 根据用户ID查找用户的分组文档
    Group findByUserId(Long userId);
} 