package com.qiu.qoj.document.model.entity;

import lombok.Data;

@Data
public class AnkiInfo {
    private Long noteId;          // Anki笔记ID
    private Long cardId;          // Anki卡片ID
    private String deckName;      // 牌组名称
    private String modelName;     // 卡片模板名称
    private Long syncTime;        // 最后与Anki同步时间（Unix时间戳，秒）
}
