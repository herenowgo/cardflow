package com.qiu.cardflow.card.interfaces;


import com.qiu.cardflow.common.interfaces.exception.BusinessException;
import com.qiu.cardflow.common.interfaces.exception.RPC;
import org.springframework.data.domain.Page;

import javax.smartcardio.Card;
import java.util.List;

public interface ICardRPC extends RPC {

    boolean createCard(CardAddRequest cardAddRequest) throws BusinessException;

    boolean deleteCard(String cardId) throws BusinessException;

    boolean updateCardContent(CardUpdateRequest cardUpdateRequest) throws BusinessException;

    AnkiSyncResponse syncWithAnki(String group) throws BusinessException;

    List<Card> getUserCards() throws BusinessException;

    List<Card> getUserGroupCards(String group) throws BusinessException;

    Page<Card> getUserGroupCardsWithPagination(String group, int page, int size) throws BusinessException;

    Card getCardById(String cardId) throws BusinessException;

    List<Card> getCardsByIds(List<String> cardIds) throws BusinessException;

    List<Card> getCardsByAnkiCardIds(List<Long> cardIds) throws BusinessException;
}