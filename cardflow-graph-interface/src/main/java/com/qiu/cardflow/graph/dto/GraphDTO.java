package com.qiu.cardflow.graph.dto;

import lombok.Data;

import java.util.List;

@Data
public class GraphDTO {
    private List<NodeDTO> nodes;

    private List<EdgeDTO> edges;
}
