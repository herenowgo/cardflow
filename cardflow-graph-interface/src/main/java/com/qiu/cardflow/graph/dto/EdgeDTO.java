package com.qiu.cardflow.graph.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class EdgeDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long source;
    private Long target;
    private int weight;
    private String name;
}
