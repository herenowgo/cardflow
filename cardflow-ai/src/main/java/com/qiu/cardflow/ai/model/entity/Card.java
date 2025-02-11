package com.qiu.cardflow.ai.model.entity;

import lombok.Data;

import java.util.List;

@Data
public class Card {
    private String question;
    private String answer;
    private List<String> tags;
}