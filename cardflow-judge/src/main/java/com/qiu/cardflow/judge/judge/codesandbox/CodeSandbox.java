package com.qiu.cardflow.judge.judge.codesandbox;

import com.qiu.cardflow.judge.judge.codesandbox.model.ExecuteCodeRequest;
import com.qiu.cardflow.judge.judge.codesandbox.model.ExecuteCodeResponse;

public interface CodeSandbox {

    /**
     * 执行代码
     *
     * @param executeCodeRequest
     * @return
     */
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);
}
