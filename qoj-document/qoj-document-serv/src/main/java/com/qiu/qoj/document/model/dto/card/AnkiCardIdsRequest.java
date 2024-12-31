package com.qiu.qoj.document.model.dto.card;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Anki卡片ID列表请求")
public class AnkiCardIdsRequest {
    @Schema(description = "Anki卡片ID列表", example = "[123, 456, 789]")
    @NotEmpty(message = "Anki卡片ID列表不能为空")
    private List<Long> cardIds;
}