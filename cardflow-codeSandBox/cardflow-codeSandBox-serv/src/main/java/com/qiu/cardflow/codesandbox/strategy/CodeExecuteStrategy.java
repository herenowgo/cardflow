package com.qiu.cardflow.codesandbox.strategy;

import com.qiu.cardflow.codesandbox.constant.ProgrammingLanguage;
import com.qiu.cardflow.codesandbox.model.ExecuteCodeRequest;
import com.qiu.cardflow.codesandbox.model.ExecuteCodeResponse;

public interface CodeExecuteStrategy {

    ProgrammingLanguage getProgrammingLanguage();

    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) throws Exception;
}
