package com.qiu.cardflow.card.model.dto.card;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CardIdsRequest {
    @NotEmpty(message = "卡片ID列表不能为空")
    private List<String> cardIds;
} 