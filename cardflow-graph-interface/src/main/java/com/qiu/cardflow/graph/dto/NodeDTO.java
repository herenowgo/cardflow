package com.qiu.cardflow.graph.dto;

import com.qiu.cardflow.graph.constants.NodeType;
import lombok.Data;

@Data
public class NodeDTO {
    private Long id;
    private String name;
    private String value;
    private NodeType type;
}
