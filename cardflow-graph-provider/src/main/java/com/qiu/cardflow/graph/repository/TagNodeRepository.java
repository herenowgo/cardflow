package com.qiu.cardflow.graph.repository;

import com.qiu.cardflow.graph.model.entity.TagNode;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagNodeRepository extends CrudRepository<TagNode, String> {

} 