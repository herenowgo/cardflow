package com.qiu.cardflow.graph.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class GraphDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private List<NodeDTO> nodes;
    private List<EdgeDTO> edges;
}
