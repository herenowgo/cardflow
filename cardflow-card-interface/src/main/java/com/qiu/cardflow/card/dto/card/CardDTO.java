package com.qiu.cardflow.card.dto.card;

import com.qiu.cardflow.card.dto.anki.AnkiInfo;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class CardDTO implements Serializable {

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
    private FSRSCard fsrsCard;
}
