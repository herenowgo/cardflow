package com.qiu.cardflow.api.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import com.github.xiaoymin.knife4j.core.util.StrUtil;
import com.qiu.cardflow.api.context.UserContext;
import com.qiu.cardflow.api.service.ICardService;
import com.qiu.cardflow.card.dto.anki.AnkiSyncResponse;
import com.qiu.cardflow.card.dto.card.CardAddRequest;
import com.qiu.cardflow.card.dto.card.CardDTO;
import com.qiu.cardflow.card.dto.card.CardPageRequest;
import com.qiu.cardflow.card.dto.card.CardUpdateRequest;
import com.qiu.cardflow.card.dto.card.ReviewLogDTO;
import com.qiu.cardflow.card.interfaces.ICardRPC;
import com.qiu.cardflow.common.interfaces.exception.BusinessException;
import com.qiu.cardflow.common.interfaces.exception.PageResult;
import com.qiu.cardflow.graph.dto.CardNodeDTO;
import com.qiu.cardflow.graph.interfaces.IGraphRpc;
import com.qiu.cardflow.graph.message.MessageWithUserId;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardServiceImpl implements ICardService {

    @DubboReference
    private ICardRPC cardRPC;

    @DubboReference
    private IGraphRpc graphRPC;

    private final StreamBridge streamBridge;

    @Override
    public Boolean createCard(CardAddRequest cardAddRequest) throws BusinessException {

        String cardId = cardRPC.createCard(cardAddRequest);
        CardNodeDTO cardNoteDTO = CardNodeDTO.builder()
                .cardId(cardId)
                .tags(cardAddRequest.getTags())
                .build();

        streamBridge.send("cardNodeGenerate-out-0",
                MessageWithUserId.builder().userId(UserContext.getUserId()).data(List.of(cardNoteDTO)).build());
        return true;
    }

    @Override
    public Boolean deleteCard(String cardId) throws BusinessException {
        cardRPC.deleteCard(cardId);
        graphRPC.deleteCard(cardId);
        return true;
    }

    @Override
    public Boolean updateCardContent(CardUpdateRequest cardUpdateRequest) throws BusinessException {
        return cardRPC.updateCardContent(cardUpdateRequest);
    }

    @Override
    public AnkiSyncResponse syncWithAnki(String group) throws BusinessException {
        return cardRPC.syncWithAnki(group);
    }

    @Override
    public List<CardDTO> getUserCards() throws BusinessException {
        return cardRPC.getUserCards();
    }

    @Override
    public List<CardDTO> getUserGroupCards(String group) throws BusinessException {
        return cardRPC.getUserGroupCards(group);
    }

    @Override
    public PageResult<CardDTO> getUserGroupCardsWithPagination(String group, int page, int size)
            throws BusinessException {
        return cardRPC.getUserGroupCardsWithPagination(group, page, size);
    }

    @Override
    public CardDTO getCardById(String cardId) throws BusinessException {
        return cardRPC.getCardById(cardId);
    }

    @Override
    public List<CardDTO> getCardsByIds(List<String> cardIds) throws BusinessException {
        return cardRPC.getCardsByIds(cardIds);
    }

    @Override
    public List<CardDTO> getCardsByAnkiCardIds(List<Long> cardIds) throws BusinessException {
        return cardRPC.getCardsByAnkiCardIds(cardIds);
    }

    @Override
    public List<ReviewLogDTO> getReviewLogsByCardId(String cardId) throws BusinessException {
        return cardRPC.getReviewLogsByCardId(cardId);
    }

    // @Override
    // public void saveReviewLog(ReviewLogDTO reviewLog) throws BusinessException {
    // cardRPC.saveReviewLog(reviewLog);
    // }

    @Override
    public void saveReviewLogs(List<ReviewLogDTO> reviewLogs) throws BusinessException {
        cardRPC.saveReviewLogs(reviewLogs);
    }

    @Override
    public List<CardDTO> getExpiredCards() throws BusinessException {
        return cardRPC.getExpiredCards();
    }

    @Override
    public List<String> saveCards(List<CardUpdateRequest> cardUpdateRequests) {
        List<String> savedCardIds = cardRPC.saveCards(cardUpdateRequests);
        List<Integer> newCardIndexs = new ArrayList<>();
        for (int i = 0; i < cardUpdateRequests.size(); i++) {
            if (StrUtil.isBlank(cardUpdateRequests.get(i).getId())) {
                newCardIndexs.add(i);
            }
        }
        if (newCardIndexs.isEmpty()) {
            return savedCardIds;
        }
        List<CardNodeDTO> newCardNodeList = new ArrayList<>();
        for (int index : newCardIndexs) {
            String cardId = savedCardIds.get(index);
            CardNodeDTO cardNodeDTO = CardNodeDTO.builder()
                    .cardId(cardId)
                    .tags(cardUpdateRequests.get(index).getTags())
                    .build();
            newCardNodeList.add(cardNodeDTO);
        }

        streamBridge.send("cardNodeGenerate-out-0",
                MessageWithUserId.builder().userId(UserContext.getUserId()).data(newCardNodeList).build());

        return savedCardIds;
    }

    @Override
    public Boolean setCardOvert(String cardId) throws BusinessException {
        CardDTO cardDTO = cardRPC.setCardOvert(cardId);
        Long userId = UserContext.getUserId();
        UserContext.setUserId(-1l);
        graphRPC.addCard(CardNodeDTO.builder().cardId(cardDTO.getId()).tags(cardDTO.getTags()).build());
        UserContext.setUserId(userId);
        return true;
    }

    @Override
    public PageResult<CardDTO> getCardsWithPagination(CardPageRequest cardPageRequest) throws BusinessException {
        return cardRPC.getCardsWithPagination(cardPageRequest);
    }

    @Override
    public Boolean deleteCardsByGroup(String groupName) {
        //创建一个新线程删除知识图谱中对应的卡片
        new Thread(() -> {
            try {
                List<CardDTO> userGroupCards = cardRPC.getUserGroupCards(groupName);
                for (CardDTO card : userGroupCards) {
                    graphRPC.deleteCard(card.getId());
                }
            } catch (Exception e) {
                log.error("Error deleting cards from graph service for group: {}", groupName, e);
            }
        }).start();
        return cardRPC.deleteCardsByGroup(groupName);
    }

}
