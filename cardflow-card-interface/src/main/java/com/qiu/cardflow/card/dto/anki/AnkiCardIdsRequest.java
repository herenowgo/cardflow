package com.qiu.cardflow.card.dto.anki;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class AnkiCardIdsRequest implements Serializable {
    @NotEmpty(message = "Anki卡片ID列表不能为空")
    private List<Long> cardIds;
}