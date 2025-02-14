package com.qiu.cardflow.graph.dto;

import com.qiu.cardflow.graph.constants.NodeType;
import lombok.Data;
import java.io.Serializable;

@Data
public class NodeDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String name;
    private String value;
    private NodeType type;
}
