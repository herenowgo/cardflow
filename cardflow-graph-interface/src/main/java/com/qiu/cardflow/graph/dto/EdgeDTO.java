package com.qiu.cardflow.graph.dto;

import lombok.Data;

@Data
public class EdgeDTO {
    private Long source;
    private Long target;
    private int weight;
    private String name;
}
