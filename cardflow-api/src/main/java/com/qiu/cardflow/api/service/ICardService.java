package com.qiu.cardflow.api.service;

import java.util.List;

import com.qiu.cardflow.card.dto.anki.AnkiSyncResponse;
import com.qiu.cardflow.card.dto.card.CardAddRequest;
import com.qiu.cardflow.card.dto.card.CardDTO;
import com.qiu.cardflow.card.dto.card.CardUpdateRequest;
import com.qiu.cardflow.card.dto.card.ReviewLogDTO;
import com.qiu.cardflow.common.interfaces.exception.BusinessException;
import com.qiu.cardflow.common.interfaces.exception.PageResult;

public interface ICardService {

    Boolean createCard(CardAddRequest cardAddRequest) throws BusinessException;

    Boolean deleteCard(String cardId) throws BusinessException;

    Boolean updateCardContent(CardUpdateRequest cardUpdateRequest) throws BusinessException;

    AnkiSyncResponse syncWithAnki(String group) throws BusinessException;

    List<CardDTO> getUserCards() throws BusinessException;

    List<CardDTO> getUserGroupCards(String group) throws BusinessException;

    PageResult<CardDTO> getUserGroupCardsWithPagination(String group, int page, int size) throws BusinessException;

    CardDTO getCardById(String cardId) throws BusinessException;

    List<CardDTO> getCardsByIds(List<String> cardIds) throws BusinessException;

    List<CardDTO> getCardsByAnkiCardIds(List<Long> cardIds) throws BusinessException;

    // ReviewLog related interfaces
    List<ReviewLogDTO> getReviewLogsByCardId(String cardId) throws BusinessException;

    void saveReviewLog(ReviewLogDTO reviewLog) throws BusinessException;

    void saveReviewLogs(List<ReviewLogDTO> reviewLogs) throws BusinessException;

    Boolean updateCards(List<CardUpdateRequest> cardUpdateRequests) throws BusinessException;
}
