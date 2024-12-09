package com.qiu.qoj.document.model.dto.card;

import com.qiu.qoj.document.model.entity.AnkiInfo;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CardAddRequest {

    private AnkiInfo ankiInfo;  // Anki相关信息
    @NotNull
    private String question;    // 问题/正面内容
    @NotNull
    private String answer;      // 答案/背面内容
    private List<String> tags;  // 标签列表
    private String group = "Default";       // 所属分组

}

