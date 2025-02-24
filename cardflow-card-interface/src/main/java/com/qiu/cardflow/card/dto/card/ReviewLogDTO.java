package com.qiu.cardflow.card.dto.card;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewLogDTO implements Serializable {
    @Schema(description = "关联的卡片ID")
    private String cardId;

    @Schema(description = "复习的评级（手动变更，重来，困难，良好，容易）")
    private String rating;
    
    @Schema(description = "复习的状态（新卡片、学习中、复习中、重新学习中）")
    private String state;
    
    @Schema(description = "上次的调度日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date due;
    
    @Schema(description = "复习前的记忆稳定性")
    private Float stability;
    
    @Schema(description = "复习前的卡片难度")
    private Float difficulty;
    
    @Schema(description = "自上次复习以来的天数")
    private Integer elapsed_days;
    
    @Schema(description = "上次复习的间隔天数")
    private Integer last_elapsed_days;
    
    @Schema(description = "下次复习的间隔天数")
    private Integer scheduled_days;
    
    @Schema(description = "复习的日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date review;
}