package com.qiu.cardflow.card.dto.card;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class CardIdsRequest implements Serializable {
    @NotEmpty(message = "卡片ID列表不能为空")
    private List<String> cardIds;
} 