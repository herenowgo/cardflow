package com.qiu.cardflow.document.model.dto.card;

import com.qiu.cardflow.document.model.entity.AnkiInfo;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CardUpdateRequest {
    @NotNull
    private String id;          // MongoDB的_id

    private AnkiInfo ankiInfo;  // Anki相关信息
    private String question;    // 问题/正面内容
    private String answer;      // 答案/背面内容
    private List<String> tags;  // 标签列表
    private String group;       // 所属分组

}

