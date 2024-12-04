package com.qiu.qoj.judge.judge.codesandbox;

import com.qiu.qoj.judge.judge.codesandbox.model.ExecuteCodeRequest;
import com.qiu.qoj.judge.judge.codesandbox.model.ExecuteCodeResponse;

public interface CodeSandbox {

    /**
     * 执行代码
     *
     * @param executeCodeRequest
     * @return
     */
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);
}
