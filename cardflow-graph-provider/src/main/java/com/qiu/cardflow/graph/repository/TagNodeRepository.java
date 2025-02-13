package com.qiu.cardflow.graph.repository;

import com.qiu.cardflow.graph.model.entity.TagNode;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagNodeRepository extends ListCrudRepository<TagNode, String> {

} 