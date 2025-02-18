package com.qiu.cardflow.ai.structured;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
class Card implements Serializable {
    private String question;
    private String answer;
    private List<String> tags;
}