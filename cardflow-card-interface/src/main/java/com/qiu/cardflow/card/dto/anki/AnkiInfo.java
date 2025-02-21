package com.qiu.cardflow.card.dto.anki;

import lombok.Data;

import java.io.Serializable;

@Data
public class AnkiInfo implements Serializable {
    private Long noteId;          // Anki笔记ID
    private Long cardId;          // Anki卡片ID
    private String modelName;     // 卡片模板名称
    private Long syncTime;        // 最后与Anki同步时间（Unix时间戳，秒）
}