package com.qiu.qoj.model.vo;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class QuestionRecommendation {
    private String recommendation;

    private List<QuestionVOForRecommend> questions;
}
