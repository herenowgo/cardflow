package com.qiu.cardflow.card.dto.anki;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "Anki卡片ID列表请求")
public class AnkiCardIdsRequest implements Serializable {
    @Schema(description = "Anki卡片ID列表")
    @NotEmpty(message = "Anki卡片ID列表不能为空")
    private List<Long> cardIds;
}