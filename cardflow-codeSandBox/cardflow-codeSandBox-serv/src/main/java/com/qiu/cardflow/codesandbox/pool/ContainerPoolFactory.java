package com.qiu.cardflow.codesandbox.pool;

import com.qiu.cardflow.codesandbox.constant.ProgrammingLanguage;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ContainerPoolFactory {

    private final Map<ProgrammingLanguage, ContainerPool> poolMap = new HashMap<>();

    public ContainerPoolFactory(List<ContainerPool> containerPoolList) {
        containerPoolList.forEach(containerPool -> {
            ProgrammingLanguage programmingLanguage = containerPool.getProgrammingLanguage();
            poolMap.put(programmingLanguage, containerPool);
        });
    }

    public ContainerPool getContainerPool(ProgrammingLanguage programmingLanguage) {
        ContainerPool containerPool = poolMap.get(programmingLanguage);
        if (containerPool == null) {
            throw new IllegalArgumentException("No container pool found for programming language: " + programmingLanguage);
        }
        return containerPool;
    }
}
