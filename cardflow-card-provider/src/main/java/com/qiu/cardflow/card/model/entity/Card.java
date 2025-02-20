package com.qiu.cardflow.card.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "cards")
public class Card {
    @Id
    private String id;          // MongoDB的_id

    private Long userId;        // 用户ID
    private AnkiInfo ankiInfo;  // Anki相关信息
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
    @Field("elapsed_days")
    private Integer elapsed_days;
    @Field("scheduled_days")
    private Integer scheduled_days;
    private Integer reps;
    private Integer lapses;
    private String state;
    @Field("last_review")
    private Date last_review;

    // 复习记录
    private List<ReviewLog> reviewLogs = new ArrayList<>();


    // 工具方法：获取当前Unix时间戳（秒）
    public static Long getCurrentUnixTime() {
        return System.currentTimeMillis() / 1000;
    }

}

