package com.qiu.cardflow.codesandbox.strategy;


import com.qiu.cardflow.codesandbox.constant.ProgrammingLanguage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 代码执行策略工厂
 * 根据编程语言获取对应的代码执行策略
 */
@Component
@Slf4j
public class CodeExecuteStrategyFactory {

    private final Map<ProgrammingLanguage, CodeExecuteStrategy> strategyMap = new HashMap<>();

    public CodeExecuteStrategyFactory(List<CodeExecuteStrategy> codeExecuteStrategyList) {
        codeExecuteStrategyList.forEach(strategy -> {
            ProgrammingLanguage language = strategy.getProgrammingLanguage();
            strategyMap.put(language, strategy);
        });
    }

    /**
     * 根据编程语言获取对应的执行策略
     *
     * @param language 编程语言枚举
     * @return 对应语言的代码执行策略
     * @throws IllegalArgumentException 如果找不到对应语言的策略
     */
    public CodeExecuteStrategy getStrategy(ProgrammingLanguage language) {
        CodeExecuteStrategy strategy = strategyMap.get(language);
        if (strategy == null) {
            throw new IllegalArgumentException("不支持的编程语言: " + language);
        }
        return strategy;
    }
}