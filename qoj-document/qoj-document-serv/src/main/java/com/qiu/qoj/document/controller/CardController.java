package com.qiu.qoj.document.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.qiu.qoj.common.api.BaseResponse;
import com.qiu.qoj.common.api.UserContext;
import com.qiu.qoj.document.model.dto.card.AnkiCardIdsRequest;
import com.qiu.qoj.document.model.dto.card.AnkiSyncResponse;
import com.qiu.qoj.document.model.dto.card.CardAddRequest;
import com.qiu.qoj.document.model.dto.card.CardIdsRequest;
import com.qiu.qoj.document.model.dto.card.CardUpdateRequest;
import com.qiu.qoj.document.model.entity.Card;
import com.qiu.qoj.document.service.impl.CardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/cards")
@Slf4j
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    // 创建新卡片
    @PostMapping
    public BaseResponse<Boolean> createCard(@RequestBody @Valid CardAddRequest cardAddRequest) {
        return BaseResponse.success(cardService.createCard(cardAddRequest));
    }

    // 删除卡片
    @DeleteMapping("/{cardId}")
    public BaseResponse<Boolean> deleteCard(@PathVariable String cardId) {
        return BaseResponse.success(cardService.deleteCard(cardId));
    }

    // 更新卡片
    @PutMapping
    public BaseResponse<Boolean> updateCard(@RequestBody @Valid CardUpdateRequest cardUpdateRequest) {
        return BaseResponse.success(cardService.updateCardContent(cardUpdateRequest));
    }

    /**
     * 为了与anki进行双向同步，返回必须的信息
     * 包括系统新增的卡片以及已经同步过的卡片的一些用来双向比较的信息
     *
     * @param group 要同步的分组名称
     * @return 同步所需的信息
     */
    @GetMapping("/group/{group}/syncWithAnki")
    public BaseResponse<AnkiSyncResponse> syncWithAnki(@PathVariable String group) {
        return BaseResponse.success(cardService.syncWithAnki(UserContext.getUserId(), group));
    }

    // 获取用户的所有卡片
    @GetMapping
    public BaseResponse<List<Card>> getUserCards() {
        return BaseResponse.success(cardService.getUserCards(UserContext.getUserId()));
    }

    // 获取用户特定分组的卡片
    @GetMapping("/group/{group}")
    public BaseResponse<List<Card>> getUserGroupCards(@PathVariable String group) {
        return BaseResponse.success(cardService.getUserGroupCards(UserContext.getUserId(), group));
    }

    // 分页获取用户特定分组的卡片
    @GetMapping("/group/{group}/page")
    public BaseResponse<Page<Card>> getUserGroupCardsWithPagination(
            @PathVariable String group,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return BaseResponse.success(
                cardService.getUserGroupCardsWithPagination(
                        UserContext.getUserId(),
                        group,
                        page,
                        size));
    }

    // 获取指定ID的卡片
    @GetMapping("/{cardId}")
    public BaseResponse<Card> getCardById(@PathVariable String cardId) {
        return BaseResponse.success(cardService.getCardById(cardId));
    }

    // 批量获取指定ID的卡片
    @PostMapping("/batch")
    public BaseResponse<List<Card>> getCardsByIds(@RequestBody @Valid CardIdsRequest request) {
        return BaseResponse.success(cardService.getCardsByIds(request.getCardIds()));
    }

    /**
     * 根据Anki卡片ID列表获取对应的卡片
     * 
     * @param request Anki卡片ID列表
     * @return 包含这些Anki卡片ID的Card列表
     */
    @Operation(summary = "根据Anki卡片ID获取卡片", description = "传入一组Anki卡片ID，返回对应的卡片列表")
    @PostMapping("/anki/cards")
    public BaseResponse<List<Card>> getCardsByAnkiCardIds(
            @Parameter(description = "Anki卡片ID列表", required = true) @RequestBody @Valid AnkiCardIdsRequest request) {
        return BaseResponse.success(cardService.getCardsByAnkiCardIds(request.getCardIds()));
    }


    // /**
    // * 检查一组Anki卡片ID是否存在
    // *
    // * @param cardIds Anki卡片ID列表
    // * @return 布尔数组，表示对应位置的Anki卡片ID是否存在
    // */
    // @Operation(summary = "检查Anki卡片是否存在", description =
    // "传入一组Anki卡片ID，返回每个ID是否存在的布尔数组")
    // @PostMapping("/anki/exists")
    // public BaseResponse<List<Boolean>> checkAnkiCardsExist(
    // @Parameter(description = "Anki卡片ID列表", required = true) @RequestBody @Valid
    // AnkiCardIdsRequest request) {
    // return
    // BaseResponse.success(cardService.checkAnkiCardsExist(request.getCardIds()));
    // }

}
