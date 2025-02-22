package com.qiu.cardflow.api.controller;

import com.qiu.cardflow.api.common.BaseResponse;
import com.qiu.cardflow.api.service.ICardService;
import com.qiu.cardflow.card.dto.anki.AnkiCardIdsRequest;
import com.qiu.cardflow.card.dto.anki.AnkiSyncResponse;
import com.qiu.cardflow.card.dto.card.*;
import com.qiu.cardflow.common.interfaces.exception.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
@Slf4j
@RequiredArgsConstructor
public class CardController {

    private final ICardService cardService;

    // 创建新卡片
    @PostMapping
    @Operation(summary = "创建卡片", description = "创建一个新的卡片")
    public BaseResponse<Boolean> createCard(@RequestBody @Valid CardAddRequest cardAddRequest) {
        return BaseResponse.success(cardService.createCard(cardAddRequest));
    }

    // 删除卡片
    @DeleteMapping("/{cardId}")
    @Operation(summary = "删除卡片", description = "根据卡片ID删除卡片")
    public BaseResponse<Boolean> deleteCard(@PathVariable String cardId) {
        return BaseResponse.success(cardService.deleteCard(cardId));
    }

    // 更新卡片
    @PutMapping
    @Operation(summary = "更新卡片", description = "更新卡片内容")
    public BaseResponse<Boolean> updateCard(@RequestBody @Valid CardUpdateRequest cardUpdateRequest) {
        return BaseResponse.success(cardService.updateCardContent(cardUpdateRequest));
    }

    @PutMapping("/batch")
    @Operation(summary = "批量更新卡片", description = "批量更新多个卡片的内容")
    public BaseResponse<Boolean> updateCards(@RequestBody @Valid List<CardUpdateRequest> cardUpdateRequests) {
        return BaseResponse.success(cardService.updateCards(cardUpdateRequests));
    }

    /**
     * 为了与anki进行双向同步，返回必须的信息
     * 包括系统新增的卡片以及已经同步过的卡片的一些用来双向比较的信息
     *
     * @param group 要同步的分组名称
     * @return 同步所需的信息
     */
    @GetMapping("/group/{group}/syncWithAnki")
    @Operation(summary = "与Anki同步", description = "根据分组名称，获取与Anki同步所需的信息")
    public BaseResponse<AnkiSyncResponse> syncWithAnki(@PathVariable String group) {
        return BaseResponse.success(cardService.syncWithAnki(group));
    }

    // 获取用户的所有卡片
    @GetMapping
    @Operation(summary = "获取用户的所有卡片", description = "获取当前用户的所有卡片")
    public BaseResponse<List<CardDTO>> getUserCards() {
        return BaseResponse.success(cardService.getUserCards());
    }

    // 获取用户特定分组的卡片
    @GetMapping("/group/{group}")
    @Operation(summary = "获取用户特定分组的卡片", description = "根据分组名称，获取当前用户特定分组的卡片")
    public BaseResponse<List<CardDTO>> getUserGroupCards(@PathVariable String group) {
        return BaseResponse.success(cardService.getUserGroupCards(group));
    }

    // 分页获取用户特定分组的卡片
    @GetMapping("/group/{group}/page")
    @Operation(summary = "分页获取用户特定分组的卡片", description = "根据分组名称，分页获取当前用户特定分组的卡片")
    public BaseResponse<PageResult<CardDTO>> getUserGroupCardsWithPagination(
            @PathVariable String group,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return BaseResponse.success(
                cardService.getUserGroupCardsWithPagination(
                        group,
                        page,
                        size));
    }

    // 获取指定ID的卡片
    @GetMapping("/{cardId}")
    @Operation(summary = "获取指定ID的卡片", description = "根据卡片ID获取卡片")
    public BaseResponse<CardDTO> getCardById(@PathVariable String cardId) {
        return BaseResponse.success(cardService.getCardById(cardId));
    }

    // 批量获取指定ID的卡片
    @PostMapping("/batch")
    @Operation(summary = "批量获取指定ID的卡片", description = "根据卡片ID列表批量获取卡片")
    public BaseResponse<List<CardDTO>> getCardsByIds(@RequestBody @Valid CardIdsRequest request) {
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
    public BaseResponse<List<CardDTO>> getCardsByAnkiCardIds(
            @Parameter(description = "Anki卡片ID列表", required = true) @RequestBody @Valid AnkiCardIdsRequest request) {
        return BaseResponse.success(cardService.getCardsByAnkiCardIds(request.getCardIds()));
    }

    @Operation(summary = "获取卡片的复习日志", description = "根据卡片ID获取复习日志列表")
    @GetMapping("/{cardId}/reviewLogs")
    public BaseResponse<List<ReviewLogDTO>> getReviewLogsByCardId(@PathVariable String cardId) {
        return BaseResponse.success(cardService.getReviewLogsByCardId(cardId));
    }

    @Operation(summary = "保存卡片的复习日志", description = "保存卡片的复习日志")
    @PostMapping("/reviewLog")
    public BaseResponse<Void> saveReviewLog(@RequestBody @Valid ReviewLogDTO reviewLogDTO) {
        cardService.saveReviewLog(reviewLogDTO);
        return BaseResponse.success(null);
    }

    @Operation(summary = "批量保存卡片的复习日志", description = "批量保存卡片的复习日志")
    @PostMapping("/reviewLogs")
    public BaseResponse<Void> saveReviewLogs(@RequestBody @Valid List<ReviewLogDTO> reviewLogDTOs) {
        cardService.saveReviewLogs(reviewLogDTOs);
        return BaseResponse.success(null);
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
