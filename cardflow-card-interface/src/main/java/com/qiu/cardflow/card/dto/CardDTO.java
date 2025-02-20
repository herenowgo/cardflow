package com.qiu.cardflow.card.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CardDTO implements Serializable {

    private String id;          // MongoDB的_id

    private Long userId;        // 用户ID
    private AnkiInfoDTO ankiInfoDTO;  // Anki相关信息
    private String question;    // 问题/正面内容
    private String answer;      // 答案/背面内容
    private List<String> tags;  // 标签列表
    private String group;       // 所属分组
    private Long modifiedTime;  // 本地最后修改时间（Unix时间戳，秒）
    private Boolean isDeleted = false;  // 逻辑删除标记
    private Long deleteTime;    // 删除时间
    private Long createTime;    // 创建时间


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
    private List<ReviewLogDTO> reviewLogDTOS = new ArrayList<>();
}
