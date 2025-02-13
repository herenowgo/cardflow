package com.qiu.cardflow.graph.repository;

import com.qiu.cardflow.graph.model.entity.UserNode;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserNodeRepository extends ListCrudRepository<UserNode, Long> {
} 