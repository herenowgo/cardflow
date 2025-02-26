package com.qiu.cardflow.card.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.qiu.cardflow.card.dto.anki.AnkiSyncResponse;
import com.qiu.cardflow.card.dto.card.CardAddRequest;
import com.qiu.cardflow.card.dto.card.CardUpdateRequest;
import com.qiu.cardflow.card.model.entity.Card;
import com.qiu.cardflow.card.model.entity.ReviewLog;
import com.qiu.cardflow.common.interfaces.exception.BusinessException;

public interface ICardService {

    Boolean createCard(CardAddRequest cardAddRequest) throws BusinessException;

    Boolean deleteCard(String cardId) throws BusinessException;

    Boolean updateCardContent(CardUpdateRequest cardUpdateRequest) throws BusinessException;

    List<String> saveCards(List<CardUpdateRequest> cardUpdateRequests);

    AnkiSyncResponse syncWithAnki(String group) throws BusinessException;

    List<Card> getUserCards() throws BusinessException;

    List<Card> getUserGroupCards(String group) throws BusinessException;

    Page<Card> getUserGroupCardsWithPagination(String group, int page, int size) throws BusinessException;

    Card getCardById(String cardId) throws BusinessException;

    List<Card> getCardsByIds(List<String> cardIds) throws BusinessException;

    List<Card> getCardsByAnkiCardIds(List<Long> cardIds) throws BusinessException;

    // ReviewLog related interfaces
    List<ReviewLog> getReviewLogsByCardId(String cardId) throws BusinessException;

    void saveReviewLogs(List<ReviewLog> reviewLogs) throws BusinessException;

    /**
     * 获取所有到期的卡片
     */
    List<Card> getExpiredCards() throws BusinessException;

}
