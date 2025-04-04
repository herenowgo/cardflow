package com.qiu.cardflow.card.repository;

import com.qiu.cardflow.card.model.entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Date;
import java.util.List;

public interface CardRepository extends MongoRepository<Card, String> {

    // 根据用户ID查找
    List<Card> findByUserId(Long userId);

    // 根据Anki笔记ID查找
    Card findByAnkiInfoNoteId(Long noteId);

    // 根据Anki卡片ID查找
    Card findByAnkiInfoCardId(Long cardId);

    // 根据ID和用户ID查找
    Card findByIdAndUserIdAndIsDeletedFalse(String id, Long userId);

    // 根据用户ID和分组查找
    List<Card> findByUserIdAndGroup(Long userId, String group);

    // 修改现有的查询方法，添加isDeleted条件
    List<Card> findByUserIdAndIsDeletedFalse(Long userId);

    List<Card> findByUserIdAndGroupAndIsDeletedFalse(Long userId, String group);

    // 获取用户未同步到Anki的卡片（没有cardId的卡片）
    @Query(value = "{ 'userId': ?0, 'isDeleted': false, '$or': [{'ankiInfo.cardId': null}, {'ankiInfo.cardId': { $exists: false }}]}")
    List<Card> findUnsynchronizedCardsByUserId(Long userId);

    // 分页获取用户特定分组的卡片
    Page<Card> findByUserIdAndGroupAndIsDeletedFalse(Long userId, String group, Pageable pageable);

    // 获取特定分组的卡片同步信息
    @Query(value = "{ 'userId': ?0, 'group': ?1, 'isDeleted': false, 'ankiInfo.cardId': { $exists: true } }", fields = "{ 'ankiInfo.cardId': 1, 'ankiInfo.syncTime': 1, 'modifiedTime': 1, 'fsrsCard.due': 1 }")
    List<Card> findCardSyncInfoByUserIdAndGroup(Long userId, String group);

    // 获取特定分组未同步到Anki的卡片
    @Query(value = "{ 'userId': ?0, 'group': ?1, 'isDeleted': false, '$or': [{'ankiInfo.cardId': null}, {'ankiInfo.cardId': { $exists: false }}]}")
    List<Card> findUnsynchronizedCardsByUserIdAndGroup(Long userId, String group);

    // 根据ID查找未删除的卡片
    Card findByIdAndIsDeletedFalse(String id);

    // 批量查询未删除的卡片
    List<Card> findByIdInAndIsDeletedFalse(List<String> ids);

    /**
     * 查找一组Anki卡片ID中存在的ID列表
     *
     * @param cardIds Anki卡片ID列表
     * @return 存在的Anki卡片ID列表
     */
    @Query(value = "{ 'ankiInfo.cardId': { $in: ?0 } }", fields = "{ 'ankiInfo.cardId': 1 }")
    List<Long> findAnkiCardIdsByCardIdIn(List<Long> cardIds);

    /**
     * 根据Anki卡片ID列表查找卡片
     *
     * @param cardIds Anki卡片ID列表
     * @return 包含这些Anki卡片ID的Card列表
     */
    @Query(value = "{ 'ankiInfo.cardId': { $in: ?0 }, 'isDeleted': false }")
    List<Card> findByAnkiInfoCardIdIn(List<Long> cardIds);

    /**
     * 获取指定用户在指定时间之前到期的卡片
     * 
     * @param userId 用户ID
     * @param date   指定的时间
     * @return 在指定时间之前到期的卡片列表
     */
    @Query(value = "{ 'userId': ?0, 'isDeleted': false, 'fsrsCard.due': { $lt: ?1 }}")
    List<Card> findOverdueCardsByUserIdAndDate(Long userId, Date date);

    /**
     * 多条件分页查询卡片
     * 
     * @param criteria 查询条件
     * @param pageable 分页参数
     * @return 符合条件的卡片分页结果
     */
    @Query("{ $and: ?0 }")
    Page<Card> findByMultipleCriteria(List<org.bson.Document> criteria, Pageable pageable);
}