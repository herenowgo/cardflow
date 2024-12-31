package com.qiu.qoj.document.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.qiu.qoj.common.api.UserContext;
import com.qiu.qoj.common.exception.Asserts;
import com.qiu.qoj.document.model.dto.card.AnkiNoteAddRequest;
import com.qiu.qoj.document.model.dto.card.AnkiSyncResponse;
import com.qiu.qoj.document.model.dto.card.CardAddRequest;
import com.qiu.qoj.document.model.dto.card.CardUpdateRequest;
import com.qiu.qoj.document.model.entity.AnkiInfo;
import com.qiu.qoj.document.model.entity.Card;
import com.qiu.qoj.document.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;

    private final GroupService groupService;

    // 更新卡片
    public Boolean updateCardContent(CardUpdateRequest cardUpdateRequest) {
        String id = cardUpdateRequest.getId();
        Card card = cardRepository.findByIdAndUserIdAndIsDeletedFalse(id, UserContext.getUserId());
        Asserts.failIf(card == null, "Card not found");

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
            card.setModifiedTime(Card.getCurrentUnixTime());
        }

        cardRepository.save(card);
        return true;
    }

    // 获取用户的所有卡片
    public List<Card> getUserCards(Long userId) {
        return cardRepository.findByUserIdAndIsDeletedFalse(userId);
    }

    // 获取用户特定分组的卡片
    public List<Card> getUserGroupCards(Long userId, String group) {
        return cardRepository.findByUserIdAndGroupAndIsDeletedFalse(userId, group);
    }

    // 分页获取用户特定分组的卡片
    public Page<Card> getUserGroupCardsWithPagination(Long userId, String group, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return cardRepository.findByUserIdAndGroupAndIsDeletedFalse(userId, group, pageable);
    }

    // 创建新卡片
    public Boolean createCard(CardAddRequest cardAddRequest) {
        Card card = new Card();
        BeanUtil.copyProperties(cardAddRequest, card);
        // 判断是系统的新卡还是anki的新卡
        if (cardAddRequest.getAnkiInfo() != null && cardAddRequest.getAnkiInfo().getSyncTime() != null) {
            card.setCreateTime(cardAddRequest.getAnkiInfo().getSyncTime());
            card.setModifiedTime(cardAddRequest.getAnkiInfo().getSyncTime());
        } else {
            Long currentUnixTime = Card.getCurrentUnixTime();
            card.setModifiedTime(currentUnixTime);
            card.setCreateTime(currentUnixTime);
        }

        card.setUserId(UserContext.getUserId());

        // 检查牌组是否存在，不存在则创建
        List<String> userGroups = groupService.getUserGroups(UserContext.getUserId());
        if (!userGroups.contains(card.getGroup())) {
            groupService.addGroup(UserContext.getUserId(), card.getGroup());
        }

        cardRepository.save(card);
        return true;
    }

    // 逻辑删除卡片
    public Boolean deleteCard(String cardId) {

        // todo 性能优化：只获取userID
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        // 检查权限
        Asserts.failIf(!UserContext.getUserId().equals(card.getUserId()) && !UserContext.isAdmin(), "没有权限删除此卡片");

        card.setIsDeleted(true);
        card.setDeleteTime(Card.getCurrentUnixTime());
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
    public AnkiSyncResponse syncWithAnki(Long userId, String group) {
        // 获取该分组下所有已经与Anki同步过的卡片
        List<Card> cards = cardRepository.findCardSyncInfoByUserIdAndGroup(userId, group);
        List<AnkiSyncResponse.AnkiSyncedCard> ankiSyncedCards = cards.stream()
                .map(card -> AnkiSyncResponse.AnkiSyncedCard.builder()
                        .id(card.getId())
                        .cardId(card.getAnkiInfo().getCardId())
                        .syncTime(card.getAnkiInfo().getSyncTime())
                        .modifiedTime(card.getModifiedTime())
                        .build())
                .collect(Collectors.toList());

        List<Long> cardIds = cards.stream()
                .map(card -> card.getAnkiInfo().getCardId())
                .collect(Collectors.toList());

        // 获取该分组下未同步的卡片
        List<Card> unsynchronizedCards = cardRepository.findUnsynchronizedCardsByUserIdAndGroup(userId, group);
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
        Asserts.failIf(card == null, "卡片不存在");

        // 检查权限：普通用户只能查看自己的卡片，管理员可以查看所有卡片
        Asserts.failIf(!UserContext.getUserId().equals(card.getUserId()) && !UserContext.isAdmin(),
                "没有权限查看此卡片");

        return card;
    }

    // 批量获取指定ID的卡片
    public List<Card> getCardsByIds(List<String> cardIds) {
        List<Card> cards = cardRepository.findByIdInAndIsDeletedFalse(cardIds);

        // 检查权限：普通用户只能查看自己的卡片，管理员可以查看所有卡片
        if (!UserContext.isAdmin()) {
            cards = cards.stream()
                    .filter(card -> card.getUserId().equals(UserContext.getUserId()))
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

}