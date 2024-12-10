package com.qiu.qoj.document.repository;

import com.qiu.qoj.document.model.entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

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

    // 根据牌组名称查找
    List<Card> findByAnkiInfoDeckName(String deckName);

    // 根据标签查找
    List<Card> findByTagsContaining(String tag);

    // 根据分组查找
    List<Card> findByGroup(String group);

    // 根据最后修改时间查找
    List<Card> findByModifiedTimeAfter(Long timestamp);

    // 根据用户ID和分组查找
    List<Card> findByUserIdAndGroup(Long userId, String group);

    // 修改现有的查询方法，添加isDeleted条件
    List<Card> findByUserIdAndIsDeletedFalse(Long userId);
    
    List<Card> findByUserIdAndGroupAndIsDeletedFalse(Long userId, String group);

    // 获取用户的卡片同步信息
    @Query(value = "{ 'userId': ?0, 'isDeleted': false, 'ankiInfo.cardId': { $exists: true } }", 
           fields = "{ 'ankiInfo.cardId': 1, 'ankiInfo.syncTime': 1, 'modifiedTime': 1 }")
    List<Card> findCardSyncInfoByUserId(Long userId);

    // 获取用户未同步到Anki的卡片（没有cardId的卡片）
    @Query(value = "{ 'userId': ?0, 'isDeleted': false, '$or': [{'ankiInfo.cardId': null}, {'ankiInfo.cardId': { $exists: false }}]}")
    List<Card> findUnsynchronizedCardsByUserId(Long userId);

    // 分页获取用户特定分组的卡片
    Page<Card> findByUserIdAndGroupAndIsDeletedFalse(Long userId, String group, Pageable pageable);

}