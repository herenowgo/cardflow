package com.qiu.cardflow.card.dto.card;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * FSRS (Free Spaced Repetition System) 卡片实体类
 * 用于表示一张用于间隔重复学习的卡片及其学习状态
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "FSRS卡片实体")
public class FSRSCard implements Serializable {

    /**
     * 卡片下次复习的日期
     */
    @Schema(description = "卡片下次复习的日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date due;

    /**
     * 记忆稳定性
     * 表示记忆的牢固程度
     */
    @Schema(description = "记忆稳定性")
    private Double stability;

    /**
     * 卡片难度
     * 表示卡片的难易程度
     */
    @Schema(description = "卡片难度")
    private Double difficulty;

    /**
     * 自上次复习以来的天数
     */
    @Schema(description = "自上次复习以来的天数")
    private Integer elapsed_days;

    /**
     * 下次复习的间隔天数
     */
    @Schema(description = "下次复习的间隔天数")
    private Integer scheduled_days;

    /**
     * 卡片被复习的总次数
     */
    @Schema(description = "卡片被复习的总次数")
    private Integer reps;

    /**
     * 卡片被遗忘或错误记忆的次数
     */
    @Schema(description = "卡片被遗忘或错误记忆的次数")
    private Integer lapses;

    /**
     * 卡片的当前状态
     * 可能的值包括：新卡片、学习中、复习中、重新学习中
     */
    @Schema(description = "卡片的当前状态（新卡片、学习中、复习中、重新学习中）")
    private String state;

    /**
     * 最近一次复习的日期
     */
    @Schema(description = "最近一次复习的日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date last_review;
}