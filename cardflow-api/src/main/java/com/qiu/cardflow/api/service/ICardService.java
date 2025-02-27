package com.qiu.cardflow.api.service;

import java.util.List;

import com.qiu.cardflow.card.dto.anki.AnkiSyncResponse;
import com.qiu.cardflow.card.dto.card.CardAddRequest;
import com.qiu.cardflow.card.dto.card.CardDTO;
import com.qiu.cardflow.card.dto.card.CardPageRequest;
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

    // void saveReviewLog(ReviewLogDTO reviewLog) throws BusinessException;

    void saveReviewLogs(List<ReviewLogDTO> reviewLogs) throws BusinessException;

    /**
     * 获取所有到期的卡片
     */
    List<CardDTO> getExpiredCards() throws BusinessException;

    List<String> saveCards(List<CardUpdateRequest> cardUpdateRequests);

    /**
     * 设置卡片为公开，仅管理员可执行此操作
     */
    Boolean setCardOvert(String cardId) throws BusinessException;

    PageResult<CardDTO> getCardsWithPagination(CardPageRequest cardPageRequest) throws BusinessException;

    // Boolean
}
