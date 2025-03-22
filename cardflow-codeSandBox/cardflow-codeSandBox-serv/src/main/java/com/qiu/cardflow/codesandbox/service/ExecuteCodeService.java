package com.qiu.cardflow.codesandbox.service;

import com.qiu.cardflow.codesandbox.model.ExecuteCodeRequest;
import com.qiu.cardflow.codesandbox.model.ExecuteCodeResponse;

public interface ExecuteCodeService {
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) throws Exception;
}
