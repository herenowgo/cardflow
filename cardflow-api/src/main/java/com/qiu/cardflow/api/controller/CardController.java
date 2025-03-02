package com.qiu.cardflow.api.controller;

import com.qiu.cardflow.api.common.BaseResponse;
import com.qiu.cardflow.api.context.UserContext;
import com.qiu.cardflow.api.service.ICardService;
import com.qiu.cardflow.api.service.impl.CardServiceImpl;
import com.qiu.cardflow.card.dto.anki.AnkiCardIdsRequest;
import com.qiu.cardflow.card.dto.anki.AnkiSyncResponse;
import com.qiu.cardflow.card.dto.card.*;
import com.qiu.cardflow.common.interfaces.exception.Assert;
import com.qiu.cardflow.common.interfaces.exception.BusinessException;
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
    @Operation(summary = "批量保存卡片", description = "批量保存多个卡片的内容，返回更新或新创建的卡片ID列表")
    public BaseResponse<List<String>> saveCards(@RequestBody @Valid List<CardUpdateRequest> cardUpdateRequests) {
        return BaseResponse.success(cardService.saveCards(cardUpdateRequests));
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

    /**
     * 多条件分页获取卡片
     * 
     * @param cardPageRequest
     * @return
     */
    @PostMapping("/page")
    @Operation(summary = "多条件分页获取卡片", description = "多条件分页获取卡片")
    public BaseResponse<PageResult<CardDTO>> getCardsWithPagination(@RequestBody CardPageRequest cardPageRequest) {
        return BaseResponse.success(
                cardService.getCardsWithPagination(cardPageRequest));
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

    // @Operation(summary = "保存卡片的复习日志", description = "保存卡片的复习日志")
    // @PostMapping("/reviewLog")
    // public BaseResponse<Void> saveReviewLog(@RequestBody @Valid ReviewLogDTO
    // reviewLogDTO) {
    // cardService.saveReviewLog(reviewLogDTO);
    // return BaseResponse.success(null);
    // }

    @Operation(summary = "批量保存卡片的复习日志", description = "批量保存卡片的复习日志")
    @PostMapping("/reviewLogs")
    public BaseResponse<Void> saveReviewLogs(@RequestBody @Valid List<ReviewLogDTO> reviewLogDTOs) {
        cardService.saveReviewLogs(reviewLogDTOs);
        return BaseResponse.success(null);
    }

    @Operation(summary = "获取已到期的卡片", description = "获取当前用户所有已到期需要复习的卡片")
    @GetMapping("/expired")
    public BaseResponse<List<CardDTO>> getExpiredCards() {
        return BaseResponse.success(cardService.getExpiredCards());
    }

    /**
     * 设置卡片为公开或私有（仅管理员可操作）
     *
     * @param cardId 卡片ID
     * @return 操作结果
     * @throws BusinessException 业务异常
     */
    @Operation(summary = "设置卡片为公开或私有", description = "设置卡片为公开或私有")
    @PostMapping("/admin/overt/{cardId}")
    public BaseResponse<Boolean> setCardOvert(@PathVariable String cardId) throws BusinessException {
        Assert.isTrue(UserContext.isAdmin(), "只有管理员才能执行此操作");
        return BaseResponse.success(cardService.setCardOvert(cardId));
    }
}
