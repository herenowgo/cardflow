package com.qiu.cardflow.card.dto.card;


import com.qiu.cardflow.card.dto.anki.AnkiInfo;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class CardUpdateRequest implements Serializable {
    private String id;          // MongoDB的_id

    private AnkiInfo ankiInfo;  // Anki相关信息
    private String question;    // 问题/正面内容
    private String answer;      // 答案/背面内容
    private List<String> tags;  // 标签列表
    private String group;       // 所属分组

    // FSRS 相关字段
    private FSRSCard fsrsCard;
}

