package com.qiu.cardflow.repository;

import com.qiu.cardflow.model.entity.Card;
import com.qiu.cardflow.model.entity.Material;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardRepository extends Neo4jRepository<Card, String> {
    List<Card> findByBasedOnMaterial(Material material);

    List<Card> findByBasedOnCard(Card card);
} 