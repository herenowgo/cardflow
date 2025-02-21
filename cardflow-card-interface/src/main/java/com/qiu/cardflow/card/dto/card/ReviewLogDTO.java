package com.qiu.cardflow.card.dto.card;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewLogDTO implements Serializable {
    private String cardId; // 关联的卡片ID

    private String rating; // 复习的评级（手动变更，重来，困难，良好，容易）
    private String state; // 复习的状态（新卡片、学习中、复习中、重新学习中）
    private Date due;  // 上次的调度日期
    private Float stability; // 复习前的记忆稳定性
    private Float difficulty; // 复习前的卡片难度
    private Integer elapsed_days; // 自上次复习以来的天数
    private Integer last_elapsed_days; // 上次复习的间隔天数
    private Integer scheduled_days; // 下次复习的间隔天数
    private Date review; // 复习的日期
}