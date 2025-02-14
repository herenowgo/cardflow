package com.qiu.cardflow.graph.repository;

import com.qiu.cardflow.graph.model.entity.CardNode;
import lombok.Data;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardNodeRepository extends ListCrudRepository<CardNode, String> {
       /**
        * 查找用户的所有卡片及标签
        */
       @Query("MATCH (u:User)-[r:CREATED]->(c:Card)-[t:HAS_TAG]->(tag:Tag) " +
                     "WHERE u.userId = $userId RETURN c, t")
       List<CardNode> findAllByUserId(Long userId);

       /**
        * 删除卡片节点及其关联关系
        */
       @Query("MATCH (c:Card {cardId: $cardId}) " +
                     "DETACH DELETE c")
       void deleteCardWithRelationships(String cardId);

       /**
        * 更新卡片的标签关系
        * 1. 保留卡片节点
        * 2. 删除旧的标签关系
        * 3. 创建新的标签关系
        */
       @Query("MATCH (c:Card {cardId: $cardId}) " +
                     "OPTIONAL MATCH (c)-[r:HAS_TAG]->(:Tag) " +
                     "DELETE r " +
                     "WITH c " +
                     "UNWIND $tagNames as tagName " +
                     "MERGE (t:Tag {name: tagName}) " +
                     "MERGE (c)-[:HAS_TAG]->(t)")
       void updateCardTags(String cardId, List<String> tagNames);

       /**
        * 获取用户所有标签及其权重
        * 返回标签名和该用户到这个标签的HAS_TAG关系数量
        */
       @Query("MATCH (u:User {userId: $userId})-[:CREATED]->(c:Card)-[r:HAS_TAG]->(t:Tag) " +
                     "RETURN t.name as tagName, count(r) as weight")
       List<TagWeightResult> findUserTagsWithWeight(Long userId);

       /**
        * 获取标签之间的共现关系及权重
        * 通过同一个卡片关联的标签建立共现关系
        */
       @Query("MATCH (u:User {userId: $userId})-[:CREATED]->(c:Card)-[:HAS_TAG]->(t1:Tag) " +
                     "MATCH (c)-[:HAS_TAG]->(t2:Tag) " +
                     "WHERE t1.name < t2.name " + // 避免重复的边
                     "RETURN t1.name as sourceTag, t2.name as targetTag, count(c) as weight")
       List<TagCoOccurrenceResult> findTagCoOccurrences(Long userId);

       @Data
       class TagWeightResult {
              String tagName;

              Long weight;

       }

       @Data
       class TagCoOccurrenceResult {
              String sourceTag;

              String targetTag;

              Long weight;
       }
}