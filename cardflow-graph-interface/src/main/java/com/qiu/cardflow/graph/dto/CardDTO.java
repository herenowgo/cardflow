package com.qiu.cardflow.graph.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class CardDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long userId;
    private String cardId;
    private List<String> tags;
}
