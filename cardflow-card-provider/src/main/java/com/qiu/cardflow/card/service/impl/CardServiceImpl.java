package com.qiu.cardflow.card.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.qiu.cardflow.card.dto.anki.AnkiInfo;
import com.qiu.cardflow.card.dto.anki.AnkiNoteAddRequest;
import com.qiu.cardflow.card.dto.anki.AnkiSyncResponse;
import com.qiu.cardflow.card.dto.card.CardAddRequest;
import com.qiu.cardflow.card.dto.card.CardDTO;
import com.qiu.cardflow.card.dto.card.CardPageRequest;
import com.qiu.cardflow.card.dto.card.CardUpdateRequest;
import com.qiu.cardflow.card.model.entity.Card;
import com.qiu.cardflow.card.model.entity.ReviewLog;
import com.qiu.cardflow.card.repository.CardRepository;
import com.qiu.cardflow.card.repository.ReviewLogRepository;
import com.qiu.cardflow.card.service.ICardService;
import com.qiu.cardflow.common.interfaces.exception.Assert;
import com.qiu.cardflow.common.interfaces.exception.BusinessException;
import com.qiu.cardflow.rpc.starter.RPCContext;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardServiceImpl implements ICardService {

    private final CardRepository cardRepository;

    private final GroupServiceImpl groupServiceImpl;

    private final ReviewLogRepository reviewLogRepository;

    // 更新卡片
    public Boolean updateCardContent(CardUpdateRequest cardUpdateRequest) {
        Card card = parseCardRequestToCard(cardUpdateRequest);

        cardRepository.save(card);
        return true;
    }

    private Card parseCardRequestToCard(CardUpdateRequest cardUpdateRequest) {
        String id = cardUpdateRequest.getId();

        if (id == null) {
            Card card = new Card();
            card.setUserId(RPCContext.getUserId());
            BeanUtil.copyProperties(cardUpdateRequest, card);
            return card;
        }
        Card card = cardRepository.findByIdAndUserIdAndIsDeletedFalse(id, RPCContext.getUserId());
        card.setUserId(RPCContext.getUserId());

        Assert.notNull(card, "Card not found");

        CopyOptions copyOptions = CopyOptions.create()
                .setIgnoreNullValue(true)
                .setIgnoreProperties(CardUpdateRequest::getAnkiInfo);
        BeanUtil.copyProperties(cardUpdateRequest, card, copyOptions);

        // Anki中对应的卡片发生了更新，要同步到系统
        if (cardUpdateRequest.getAnkiInfo() != null) {
            if (card.getAnkiInfo() == null) {
                card.setAnkiInfo(new AnkiInfo());
            }
            BeanUtil.copyProperties(cardUpdateRequest.getAnkiInfo(), card.getAnkiInfo(),
                    CopyOptions.create().setIgnoreNullValue(true));
            if (cardUpdateRequest.getAnswer() != null || cardUpdateRequest.getQuestion() != null) {
                card.setModifiedTime(cardUpdateRequest.getAnkiInfo().getSyncTime());
            }
        } else {
            // 只是系统中的卡片发生了更新
            card.setModifiedTime(com.qiu.cardflow.card.model.entity.Card.getCurrentUnixTime());
        }
        return card;
    }

    @Override
    public List<String> saveCards(List<CardUpdateRequest> cardUpdateRequests) throws BusinessException {
        List<String> groupList = cardUpdateRequests.stream()
                .map(card -> card.getGroup())
                .filter(group -> group != null)
                .distinct()
                .toList();
        // 检查牌组是否存在，不存在则创建
        List<String> userGroups = groupServiceImpl.getUserGroups();
        for (String group : groupList) {
            if (!userGroups.contains(group)) {
                groupServiceImpl.addGroup(group);
            }
        }

        List<Card> savedCards = cardRepository.saveAll(cardUpdateRequests.stream()
                .map(this::parseCardRequestToCard)
                .toList());
        return savedCards.stream()
                .map(Card::getId)
                .collect(Collectors.toList());
    }

    // 获取用户的所有卡片
    public List<Card> getUserCards() {
        return cardRepository.findByUserIdAndIsDeletedFalse(RPCContext.getUserId());
    }

    // 获取用户特定分组的卡片
    public List<Card> getUserGroupCards(String group) {
        return cardRepository.findByUserIdAndGroupAndIsDeletedFalse(RPCContext.getUserId(), group);
    }

    // 分页获取用户特定分组的卡片
    public Page<Card> getUserGroupCardsWithPagination(String group, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return cardRepository.findByUserIdAndGroupAndIsDeletedFalse(RPCContext.getUserId(), group, pageable);
    }

    // 创建新卡片
    public String createCard(CardAddRequest cardAddRequest) {
        Card card = new Card();
        BeanUtil.copyProperties(cardAddRequest, card);
        // 判断是系统的新卡还是anki的新卡
        if (cardAddRequest.getAnkiInfo() != null && cardAddRequest.getAnkiInfo().getSyncTime() != null) {
            card.setCreateTime(cardAddRequest.getAnkiInfo().getSyncTime());
            card.setModifiedTime(cardAddRequest.getAnkiInfo().getSyncTime());
        } else {
            Long currentUnixTime = com.qiu.cardflow.card.model.entity.Card.getCurrentUnixTime();
            card.setModifiedTime(currentUnixTime);
            card.setCreateTime(currentUnixTime);
        }

        card.setUserId(RPCContext.getUserId());

        // 检查牌组是否存在，不存在则创建
        List<String> userGroups = groupServiceImpl.getUserGroups();
        if (!userGroups.contains(card.getGroup())) {
            groupServiceImpl.addGroup(card.getGroup());
        }

        return cardRepository.save(card).getId();
    }

    // 逻辑删除卡片
    public Boolean deleteCard(String cardId) {

        // todo 性能优化：只获取userID
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        // 检查权限
        Assert.isTrue(RPCContext.getUserId().equals(card.getUserId()) || RPCContext.isAdmin(), "没有权限删除此卡片");
        card.setIsDeleted(true);
        card.setDeleteTime(com.qiu.cardflow.card.model.entity.Card.getCurrentUnixTime());
        cardRepository.save(card);
        return true;
    }

    /**
     * 为了与 anki 进行同步，需要获取一些用来比较的必须信息
     * 1. 新卡片的同步
     * 系统中没有cardID的卡片
     * anki中有但是系统中没有的AnkiInfo的cardID——需要系统中用户所有的AnkiInfo的cardID
     * 2. 更新过的卡片的同步
     * 判断系统中的卡片是否更新了：modifiedTime > AnkiInfo的syncTime
     * 判断anki中的卡片是否更新了：anki笔记的mod > AnkiInfo的syncTime
     * 都判断之后，如果只有一边更新了，就同步给另一端
     * 否则都显示出来，让用户去选择
     * 3. todo 删除的卡片的同步
     * anki中的删了怎么办？ 那就是anki有cardID，而我们这边没有了，那就
     * 我们这边删了，那就没有cardId， 而anki有。那建议把anki那边的删掉。
     * 1. 最好先标记，删除的时候，如果ankiInfo不为空，就标记一个要删除。然后同步的时候，
     * 3. 后端接口设计
     * 所有卡片的同步时间、AnkiInfo的cardID、和modifiedTime
     * 以及没有cardID的卡片的数据
     */

    /**
     * 与 anki 进行特定分组的同步
     */
    public AnkiSyncResponse syncWithAnki(String group) {
        // 获取该分组下所有已经与Anki同步过的卡片
        List<Card> cards = cardRepository.findCardSyncInfoByUserIdAndGroup(RPCContext.getUserId(), group);
        // todo 性能优化：只获取需要的字段
        List<AnkiSyncResponse.AnkiSyncedCard> ankiSyncedCards = cards.stream()
                .map(card -> AnkiSyncResponse.AnkiSyncedCard.builder()
                        .id(card.getId())
                        .cardId(card.getAnkiInfo().getCardId())
                        .syncTime(card.getAnkiInfo().getSyncTime())
                        .modifiedTime(card.getModifiedTime())
                        .due(card.getFsrsCard() != null ? card.getFsrsCard().getDue() : null) // 设置 due 字段
                        .build())
                .collect(Collectors.toList());

        List<Long> cardIds = cards.stream()
                .map(card -> card.getAnkiInfo().getCardId())
                .collect(Collectors.toList());

        // 获取该分组下未同步的卡片
        List<Card> unsynchronizedCards = cardRepository.findUnsynchronizedCardsByUserIdAndGroup(RPCContext.getUserId(),
                group);
        List<AnkiNoteAddRequest> ankiNoteAddRequests = unsynchronizedCards.stream()
                .map(card -> AnkiNoteAddRequest.builder()
                        .id(card.getId())
                        .question(card.getQuestion())
                        .answer(card.getAnswer())
                        .deckName(group) // 使用当前分组作为牌组名
                        .modelName("Basic")
                        .tags(card.getTags())
                        .build())
                .collect(Collectors.toList());

        return AnkiSyncResponse.builder()
                .ankiNoteAddRequests(ankiNoteAddRequests)
                .ankiSyncedCards(ankiSyncedCards)
                .cardIds(cardIds)
                .build();
    }

    // 获取指定ID的卡片
    public Card getCardById(String cardId) {
        Card card = cardRepository.findByIdAndIsDeletedFalse(cardId);
        Assert.notNull(card, "卡片不存在");
        // 检查权限：普通用户只能查看自己的卡片，管理员可以查看所有卡片
        Assert.isTrue(RPCContext.getUserId().equals(card.getUserId()) || RPCContext.isAdmin(), "没有权限查看此卡片");
        return card;
    }

    // 批量获取指定ID的卡片
    public List<Card> getCardsByIds(List<String> cardIds) {
        List<Card> cards = cardRepository.findByIdInAndIsDeletedFalse(cardIds);

        // 检查权限：普通用户只能查看自己的卡片，管理员可以查看所有卡片
        if (!RPCContext.isAdmin()) {
            cards = cards.stream()
                    .filter(card -> card.getUserId().equals(RPCContext.getUserId()))
                    .collect(Collectors.toList());
        }

        return cards;
    }

    /**
     * 检查一组Anki卡片ID是否存在
     *
     * @param ankiCardIds Anki卡片ID列表
     * @return 布尔数组，表示对应位置的Anki卡片ID是否存在
     */
    public List<Boolean> checkAnkiCardsExist(List<Long> ankiCardIds) {
        // 获取所有存在的Anki卡片ID
        List<Long> existingCardIds = cardRepository.findAnkiCardIdsByCardIdIn(ankiCardIds);

        // 将结果转换为布尔数组
        return ankiCardIds.stream()
                .map(existingCardIds::contains)
                .collect(Collectors.toList());
    }

    /**
     * 根据Anki卡片ID列表获取对应的卡片
     *
     * @param ankiCardIds Anki卡片ID列表
     * @return 包含这些Anki卡片ID的Card列表
     */
    public List<Card> getCardsByAnkiCardIds(List<Long> ankiCardIds) {
        List<Card> cards = cardRepository.findByAnkiInfoCardIdIn(ankiCardIds);

        // 检查权限：普通用户只能查看自己的卡片，管理员可以查看所有卡片
        if (!RPCContext.isAdmin()) {
            cards = cards.stream()
                    .filter(card -> card.getUserId().equals(RPCContext.getUserId()))
                    .collect(Collectors.toList());
        }

        return cards;
    }

    @Override
    public List<ReviewLog> getReviewLogsByCardId(String cardId) throws BusinessException {
        return reviewLogRepository.findByCardId(cardId);
    }

    // @Override
    // public void saveReviewLog(ReviewLog reviewLog) throws BusinessException {
    // reviewLogRepository.save(reviewLog);
    // }

    @Override
    public void saveReviewLogs(List<ReviewLog> reviewLogs) throws BusinessException {
        reviewLogRepository.saveAll(reviewLogs);
    }

    @Override
    public List<Card> getExpiredCards() throws BusinessException {
        Date now = new Date();
        return cardRepository.findOverdueCardsByUserIdAndDate(RPCContext.getUserId(), now);
    }

    @Override
    public CardDTO setCardOvert(String cardId) throws BusinessException {
        // 检查当前用户是否为管理员
        Assert.isTrue(RPCContext.isAdmin(), "只有管理员可以设置卡片为公开");

        // 获取卡片
        Card card = cardRepository.findByIdAndIsDeletedFalse(cardId);
        Assert.notNull(card, "卡片不存在");

        // 设置卡片为公开
        if (card.getOvert() == null || card.getOvert() == false) {
            card.setOvert(true);
        } else {
            card.setOvert(false);
        }
        cardRepository.save(card);

        return BeanUtil.copyProperties(card, CardDTO.class);
    }

    @Override
    public Page<Card> getCardsWithPagination(CardPageRequest cardPageRequest) throws BusinessException {
        // 创建分页参数
        Pageable pageable = PageRequest.of(
                cardPageRequest.getCurrent().intValue(),
                cardPageRequest.getPageSize().intValue());

        // 构建查询条件
        List<org.bson.Document> criteria = new ArrayList<>();

        // 基础条件：未删除的卡片
        criteria.add(new org.bson.Document("isDeleted", false));

        // 处理公开属性
        if (cardPageRequest.getOvert() != null && cardPageRequest.getOvert()) {
            criteria.add(new org.bson.Document("overt", true));
        } else {
            // 非公开卡片只能查看自己的
            criteria.add(new org.bson.Document("userId", RPCContext.getUserId()));
        }

        // 处理问题内容模糊查询
        if (cardPageRequest.getQuestion() != null && !cardPageRequest.getQuestion().isEmpty())
        {
            criteria.add(new org.bson.Document("question",
                    new org.bson.Document("$regex", cardPageRequest.getQuestion()).append("$options", "i")));
        }

        // 处理答案内容模糊查询
        if (cardPageRequest.getAnswer() != null && !cardPageRequest.getAnswer().isEmpty()) {
            criteria.add(new org.bson.Document("answer",
                    new org.bson.Document("$regex", cardPageRequest.getAnswer()).append("$options", "i")));
        }

        // 处理标签查询
        if (cardPageRequest.getTags() != null && !cardPageRequest.getTags().isEmpty()) {
            criteria.add(new org.bson.Document("tags",
                    new org.bson.Document("$all", cardPageRequest.getTags())));
        }

        // 处理分组查询
        if (cardPageRequest.getGroup() != null && !cardPageRequest.getGroup().isEmpty()) {
            criteria.add(new org.bson.Document("group", cardPageRequest.getGroup()));
        }

        // 执行查询
        Page<Card> result = cardRepository.findByMultipleCriteria(criteria, pageable);

        return result;
    }

    @Override
    public Boolean deleteCardsByGroup(String groupName) throws BusinessException {
        Long userId = RPCContext.getUserId();
        // 查找当前用户指定牌组的所有卡片
        List<Card> cards = cardRepository.findByUserIdAndGroup(userId, groupName);
        if (cards.isEmpty()) {
            return true;
        }
        // 删除找到的卡片
        cardRepository.deleteAll(cards);
        return true;
    }
}