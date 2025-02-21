package com.qiu.cardflow.card.repository;

import com.qiu.cardflow.card.model.entity.ReviewLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ReviewLogRepository extends MongoRepository<ReviewLog, String> {
    List<ReviewLog> findByCardId(String cardId);
} 