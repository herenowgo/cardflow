package com.qiu.cardflow.api.service.impl;

import java.util.List;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import com.qiu.cardflow.api.service.ICardService;
import com.qiu.cardflow.card.dto.anki.AnkiSyncResponse;
import com.qiu.cardflow.card.dto.card.CardAddRequest;
import com.qiu.cardflow.card.dto.card.CardDTO;
import com.qiu.cardflow.card.dto.card.CardUpdateRequest;
import com.qiu.cardflow.card.dto.card.ReviewLogDTO;
import com.qiu.cardflow.card.interfaces.ICardRPC;
import com.qiu.cardflow.common.interfaces.exception.BusinessException;
import com.qiu.cardflow.common.interfaces.exception.PageResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardServiceImpl implements ICardService {

    @DubboReference
    private ICardRPC cardRPC;

    @Override
    public Boolean createCard(CardAddRequest cardAddRequest) throws BusinessException {
        return cardRPC.createCard(cardAddRequest);
    }

    @Override
    public Boolean deleteCard(String cardId) throws BusinessException {
        return cardRPC.deleteCard(cardId);
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
        return cardRPC.saveCards(cardUpdateRequests);
    }

}
