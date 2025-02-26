package com.qiu.cardflow.card.rpc;

import cn.hutool.core.bean.BeanUtil;
import com.qiu.cardflow.card.dto.anki.AnkiSyncResponse;
import com.qiu.cardflow.card.dto.card.CardAddRequest;
import com.qiu.cardflow.card.dto.card.CardDTO;
import com.qiu.cardflow.card.dto.card.CardUpdateRequest;
import com.qiu.cardflow.card.dto.card.ReviewLogDTO;
import com.qiu.cardflow.card.interfaces.ICardRPC;
import com.qiu.cardflow.card.model.entity.Card;
import com.qiu.cardflow.card.model.entity.ReviewLog;
import com.qiu.cardflow.card.service.ICardService;
import com.qiu.cardflow.common.interfaces.exception.BusinessException;
import com.qiu.cardflow.common.interfaces.exception.PageResult;
import com.qiu.cardflow.rpc.starter.RPCContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.stream.Collectors;

@DubboService
@Slf4j
@RequiredArgsConstructor
@Validated
public class CardRPCImpl implements ICardRPC {

    private final ICardService cardService;


    @Override
    public Boolean createCard(CardAddRequest cardAddRequest) throws BusinessException {
        return cardService.createCard(cardAddRequest);
    }

    @Override
    public Boolean deleteCard(String cardId) throws BusinessException {
        return cardService.deleteCard(cardId);
    }

    @Override
    public Boolean updateCardContent(CardUpdateRequest cardUpdateRequest) throws BusinessException {
        return cardService.updateCardContent(cardUpdateRequest);
    }


    @Override
    public List<String> saveCards(List<CardUpdateRequest> cardUpdateRequests) {
        return cardService.saveCards(cardUpdateRequests);
    }

    @Override
    public AnkiSyncResponse syncWithAnki(String group) throws BusinessException {
        return cardService.syncWithAnki(group);
    }

    @Override
    public List<CardDTO> getUserCards() throws BusinessException {
        List<Card> userCards = cardService.getUserCards();
        return BeanUtil.copyToList(userCards, CardDTO.class);
    }

    @Override
    public List<CardDTO> getUserGroupCards(String group) throws BusinessException {
        List<Card> userGroupCards = cardService.getUserGroupCards(group);
        return BeanUtil.copyToList(userGroupCards, CardDTO.class);
    }

    @Override
    public PageResult<CardDTO> getUserGroupCardsWithPagination(String group, int page, int size) throws BusinessException {
        Page<Card> cardPage = cardService.getUserGroupCardsWithPagination(group, page, size);
        Page<CardDTO> cardDTOPage = cardPage.map(card -> BeanUtil.copyProperties(card, CardDTO.class));
        PageResult<CardDTO> pageResult = parsePage(cardDTOPage);

        return pageResult;
    }

    private <T> PageResult<T> parsePage(Page<T> cardPage) {
        return PageResult.<T>builder()
                .content(cardPage.getContent())
                .totalElements(cardPage.getTotalElements())
                .pageSize(cardPage.getSize())
                .totalPages(cardPage.getTotalPages())
                .currentPage(cardPage.getNumber())
                .build();
    }

    @Override
    public CardDTO getCardById(String cardId) throws BusinessException {
        return BeanUtil.copyProperties(cardService.getCardById(cardId), CardDTO.class);
    }

    @Override
    public List<CardDTO> getCardsByIds(List<String> cardIds) throws BusinessException {
        List<Card> cardsByIds = cardService.getCardsByIds(cardIds);
        return BeanUtil.copyToList(cardsByIds, CardDTO.class);
    }

    @Override
    public List<CardDTO> getCardsByAnkiCardIds(List<Long> cardIds) throws BusinessException {
        List<Card> cardsByAnkiCardIds = cardService.getCardsByAnkiCardIds(cardIds);
        return BeanUtil.copyToList(cardsByAnkiCardIds, CardDTO.class);
    }

    @Override
    public List<ReviewLogDTO> getReviewLogsByCardId(String cardId) throws BusinessException {
        List<ReviewLog> reviewLogsByCardId = cardService.getReviewLogsByCardId(cardId);
        return BeanUtil.copyToList(reviewLogsByCardId, ReviewLogDTO.class);
    }

    // @Override
    // public void saveReviewLog(ReviewLogDTO reviewLogDTO) throws BusinessException {
    //     ReviewLog reviewLog = BeanUtil.copyProperties(reviewLogDTO, ReviewLog.class);
    //     reviewLog.setUserId(RPCContext.getUserId());
    //     cardService.saveReviewLog(reviewLog);
    // }

    @Override
    public void saveReviewLogs(List<ReviewLogDTO> reviewLogDTOs) throws BusinessException {
        List<ReviewLog> reviewLogs = reviewLogDTOs.stream()
                .map(dto -> {
                    ReviewLog reviewLog = BeanUtil.copyProperties(dto, ReviewLog.class);
                    reviewLog.setUserId(RPCContext.getUserId());
                    return reviewLog;
                })
                .collect(Collectors.toList());
        cardService.saveReviewLogs(reviewLogs);
    }

    @Override
    public List<CardDTO> getExpiredCards() throws BusinessException {
        List<Card> expiredCards = cardService.getExpiredCards();
        return BeanUtil.copyToList(expiredCards, CardDTO.class);
    }

}
