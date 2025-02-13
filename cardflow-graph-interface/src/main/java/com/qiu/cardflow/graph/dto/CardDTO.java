package com.qiu.cardflow.graph.dto;

import lombok.Data;

import java.util.List;

@Data
public class CardDTO {
    Long userId;
    String cardId;
    List<String> tags;
}
