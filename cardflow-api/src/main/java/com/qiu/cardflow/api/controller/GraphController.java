package com.qiu.cardflow.api.controller;

import com.qiu.cardflow.api.common.BaseResponse;
import com.qiu.cardflow.api.context.UserContext;
import com.qiu.cardflow.api.service.IGraphService;
import com.qiu.cardflow.graph.dto.CardNodeDTO;
import com.qiu.cardflow.graph.dto.GraphDTO;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/graph")
@RequiredArgsConstructor
@Slf4j
@Validated
public class GraphController {

    private final IGraphService graphService;

    @PostMapping("/card")
    @Operation(summary = "添加卡片到知识图谱")
    public BaseResponse<Boolean> addCard(@RequestBody CardNodeDTO cardDTO) {
        boolean result = graphService.addCard(cardDTO);
        return BaseResponse.success(result);
    }

    @DeleteMapping("/card/{cardId}")
    @Operation(summary = "从知识图谱中删除卡片")
    public BaseResponse<Boolean> deleteCard(@PathVariable String cardId) {
        boolean result = graphService.deleteCard(cardId);
        return BaseResponse.success(result);
    }

    @PutMapping("/card")
    @Operation(summary = "更新知识图谱中的卡片")
    public BaseResponse<Boolean> updateCard(@RequestBody CardNodeDTO cardDTO) {
        boolean result = graphService.updateCard(cardDTO);
        return BaseResponse.success(result);
    }

    @GetMapping("/tags")
    @Operation(summary = "获取用户的知识标签图谱")
    public BaseResponse<GraphDTO> getTagsGraph() {
        GraphDTO graphDTO = graphService.getTagsGraph();
        return BaseResponse.success(graphDTO);
    }
}
