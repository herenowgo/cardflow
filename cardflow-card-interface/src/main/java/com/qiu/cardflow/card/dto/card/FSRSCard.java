package com.qiu.cardflow.card.dto.card;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FSRSCard implements Serializable {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") // Specify the correct format
    private Date due;
    private Float stability;
    private Float difficulty;
    private Integer elapsed_days;
    private Integer scheduled_days;
    private Integer reps;
    private Integer lapses;
    private String state;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") // Specify the correct format
    private Date last_review;
} 