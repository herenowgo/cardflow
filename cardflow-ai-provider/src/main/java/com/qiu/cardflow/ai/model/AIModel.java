package com.qiu.cardflow.ai.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AIModel {
    private String name;
    private List<AIModelInstance> instances;

    public AIModelInstance getInstance() {
        if(instances == null || instances.isEmpty()) {
            throw new RuntimeException("no AIModelInstance found from AIModel " + name);
        }
        return instances.get(0);
    }
}
