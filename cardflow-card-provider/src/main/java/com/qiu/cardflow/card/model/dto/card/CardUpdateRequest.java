package com.qiu.cardflow.card.model.dto.card;

import com.qiu.cardflow.document.model.entity.AnkiInfo;
import com.qiu.cardflow.document.model.entity.ReviewLog;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;
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

    // FSRS 相关字段
    private Date due;
    private Float stability;
    private Float difficulty;
    private Integer elapsed_days;
    private Integer scheduled_days;
    private Integer reps;
    private Integer lapses;
    private String state;
    private Date last_review;

    // 复习记录
    private List<ReviewLog> reviewLogs;

}

