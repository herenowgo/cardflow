package com.qiu.cardflow.document.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewLog {
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