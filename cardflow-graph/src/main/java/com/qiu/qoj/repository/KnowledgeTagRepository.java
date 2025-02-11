package com.qiu.cardflow.repository;

import com.qiu.cardflow.model.entity.Card;
import com.qiu.cardflow.model.entity.KnowledgeTag;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface KnowledgeTagRepository extends Neo4jRepository<KnowledgeTag, String> {
    List<KnowledgeTag> findByName(String name);

    // 查找相关的知识点
    @Query("MATCH (k:KnowledgeTag)-[:RELATED_TO]-(related) WHERE k.id = $knowledgeTagId RETURN related")
    List<KnowledgeTag> findRelatedTags(String knowledgeTagId);

    // 查找子知识点
    @Query("MATCH (k:KnowledgeTag)<-[:IS_CHILD_OF]-(child) WHERE k.id = $knowledgeTagId RETURN child")
    List<KnowledgeTag> findChildren(String knowledgeTagId);

    // 查找父知识点
    @Query("MATCH (k:KnowledgeTag)-[:IS_CHILD_OF]->(parent) WHERE k.id = $knowledgeTagId RETURN parent")
    KnowledgeTag findParent(String knowledgeTagId);

    // 查找使用了该知识点的所有卡片
    @Query("MATCH (k:KnowledgeTag)<-[:HAS_TAG]-(card:Card) WHERE k.id = $knowledgeTagId RETURN card")
    List<Card> findCardsWithTag(String knowledgeTagId);

    // 分析知识点共现关系
    @Query("""
                MATCH (k1:KnowledgeTag)<-[:HAS_TAG]-(c:Card)-[:HAS_TAG]->(k2:KnowledgeTag)
                WHERE k1 <> k2 AND k1.id = $tagId
                WITH k2, count(c) as cooccurrence
                RETURN k2, cooccurrence
                ORDER BY cooccurrence DESC
                LIMIT 10
            """)
    List<Map<String, Object>> findCooccurringTags(String tagId);

    // 查找知识点的关联路径
    @Query("""
                MATCH path = (start:KnowledgeTag)-[r:RELATED_TO*1..3]->(end:KnowledgeTag)
                WHERE start.id = $startId AND end.id = $endId
                RETURN path
            """)
    List<Map<String, Object>> findKnowledgePaths(String startId, String endId);

    // 获取知识点的完整上下文（包括相关知识点、卡片等）
    @Query("""
                MATCH (k:KnowledgeTag {id: $tagId})
                OPTIONAL MATCH (k)-[r:RELATED_TO]->(related:KnowledgeTag)
                OPTIONAL MATCH (k)<-[:HAS_TAG]-(card:Card)
                RETURN k, collect(distinct r) as relations, 
                       collect(distinct related) as relatedTags,
                       collect(distinct card) as cards
            """)
    Map<String, Object> getKnowledgeContext(String tagId);

    /**
     * 获取用户的完整知识图谱
     * 返回所有知识点及其关系
     */
    @Query("""
                MATCH (u:User {id: $userId})-[:TAGGED]->(k:KnowledgeTag)
                WITH k
                OPTIONAL MATCH (k)-[r:RELATED_TO]->(related:KnowledgeTag)
                OPTIONAL MATCH (k)-[p:IS_CHILD_OF]->(parent:KnowledgeTag)
                RETURN k, 
                       collect(distinct r) as relations,
                       collect(distinct related) as relatedTags,
                       collect(distinct p) as parentRelations,
                       collect(distinct parent) as parents
            """)
    List<Map<String, Object>> getUserKnowledgeGraph(Long userId);

    /**
     * 获取两个知识点之间的所有可能路径
     */
    @Query("""
                MATCH paths = (start:KnowledgeTag)-[r:RELATED_TO|IS_CHILD_OF*..4]-(end:KnowledgeTag)
                WHERE start.id = $startId AND end.id = $endId
                RETURN paths
            """)
    List<Map<String, Object>> findAllPathsBetweenTags(String startId, String endId);
} 