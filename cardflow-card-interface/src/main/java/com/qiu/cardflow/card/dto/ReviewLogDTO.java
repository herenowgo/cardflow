package com.qiu.cardflow.card.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewLogDTO implements Serializable {
    private String rating;
    private String state;
    private Date due;
    private Float stability;
    private Float difficulty;
    private Integer elapsed_days;
    private Integer last_elapsed_days;
    private Integer scheduled_days;
    private Date review;
}