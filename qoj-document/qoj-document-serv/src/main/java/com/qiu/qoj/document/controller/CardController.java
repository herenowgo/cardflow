package com.qiu.qoj.document.controller;

import com.qiu.qoj.common.api.BaseResponse;
import com.qiu.qoj.common.api.UserContext;
import com.qiu.qoj.document.model.dto.card.AnkiSyncResponse;
import com.qiu.qoj.document.model.dto.card.CardAddRequest;
import com.qiu.qoj.document.model.dto.card.CardUpdateRequest;
import com.qiu.qoj.document.model.entity.Card;
import com.qiu.qoj.document.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
     * @return
     */
    @GetMapping("/syncWithAnki")
    public BaseResponse<AnkiSyncResponse> syncWithAnki() {
        return BaseResponse.success(cardService.syncWithAnki());
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


}
