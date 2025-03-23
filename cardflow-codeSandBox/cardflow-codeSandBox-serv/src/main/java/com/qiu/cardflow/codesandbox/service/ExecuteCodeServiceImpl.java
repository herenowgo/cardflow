package com.qiu.cardflow.codesandbox.service;

import com.qiu.cardflow.codesandbox.constant.ProgrammingLanguage;
import com.qiu.cardflow.codesandbox.dto.ExecuteCodeRequest;
import com.qiu.cardflow.codesandbox.model.ExecuteCodeResponse;
import com.qiu.cardflow.codesandbox.strategy.CodeExecuteStrategy;
import com.qiu.cardflow.codesandbox.strategy.CodeExecuteStrategyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExecuteCodeServiceImpl implements ExecuteCodeService {

    private final CodeExecuteStrategyFactory codeExecuteStrategyFactory;

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) throws Exception {
        ProgrammingLanguage programmingLanguage = ProgrammingLanguage.fromCode(executeCodeRequest.getLanguage());
        CodeExecuteStrategy strategy = codeExecuteStrategyFactory.getStrategy(programmingLanguage);
        return strategy.executeCode(executeCodeRequest);
    }
}
