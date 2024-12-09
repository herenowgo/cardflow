package com.qiu.qoj.document.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.qiu.qoj.common.api.UserContext;
import com.qiu.qoj.common.exception.Asserts;
import com.qiu.qoj.document.model.dto.card.AnkiNoteAddRequest;
import com.qiu.qoj.document.model.dto.card.AnkiSyncResponse;
import com.qiu.qoj.document.model.dto.card.CardAddRequest;
import com.qiu.qoj.document.model.dto.card.CardUpdateRequest;
import com.qiu.qoj.document.model.entity.Card;
import com.qiu.qoj.document.repository.CardRepository;
import com.qiu.qoj.document.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
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
        BeanUtil.copyProperties(cardUpdateRequest.getAnkiInfo(), card.getAnkiInfo(), CopyOptions.create().setIgnoreNullValue(true));

        card.setModifiedTime(Card.getCurrentUnixTime());
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

    // 创建新卡片
    public Boolean createCard(CardAddRequest cardAddRequest) {
        Card card = new Card();
        BeanUtil.copyProperties(cardAddRequest, card);
        card.setModifiedTime(Card.getCurrentUnixTime());
        card.setCreateTime(Card.getCurrentUnixTime());
        card.setUserId(UserContext.getUserId());
        if (cardAddRequest.getAnkiInfo() != null && cardAddRequest.getAnkiInfo().getDeckName() != null) {
            card.setGroup(cardAddRequest.getAnkiInfo().getDeckName());
        }

        List<String> userGroups = groupService.getUserGroups(UserContext.getUserId());
        if(!userGroups.contains(card.getGroup())) {
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
     * 3. 后端接口设计
     * 所有卡片的同步时间、AnkiInfo的cardID、和modifiedTime
     * 以及没有cardID的卡片的数据
     */
    public AnkiSyncResponse syncWithAnki() {
        // 获取所有已经与Anki同步过的卡片
        List<Card> cards = cardRepository.findCardSyncInfoByUserId(UserContext.getUserId());
        List<AnkiSyncResponse.AnkiSyncedCard> ankiSyncedCards = cards.stream()
                .map(card -> {
                    return AnkiSyncResponse.AnkiSyncedCard.builder()
                            .cardId(card.getAnkiInfo().getCardId())
                            .syncTime(card.getAnkiInfo().getSyncTime())
                            .modifiedTime(card.getModifiedTime())
                            .build();
                })
                .collect(Collectors.toList());

        List<Long> cardIds = cards.stream()
                .map(card -> {
                    return card.getAnkiInfo().getCardId();
                })
                .collect(Collectors.toList());

        List<Card> unsynchronizedCards = cardRepository.findUnsynchronizedCardsByUserId(UserContext.getUserId());
        List<AnkiNoteAddRequest> ankiNoteAddRequests = unsynchronizedCards.stream()
                .map(card -> {
                    return AnkiNoteAddRequest.builder()
                            .question(card.getQuestion())
                            .answer(card.getAnswer())
                            .deckName(card.getAnkiInfo().getDeckName())
                            .modelName(card.getAnkiInfo().getModelName())
                            .tags(card.getTags())
                            .build();
                })
                .collect(Collectors.toList());

        return AnkiSyncResponse.builder()
                .ankiNoteAddRequests(ankiNoteAddRequests)
                .ankiSyncedCards(ankiSyncedCards)
                .cardIds(cardIds)
                .build();
    }


}