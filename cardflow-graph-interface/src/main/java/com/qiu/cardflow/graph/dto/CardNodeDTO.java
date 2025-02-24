package com.qiu.cardflow.graph.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class CardNodeDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String cardId;
    private List<String> tags;
}
