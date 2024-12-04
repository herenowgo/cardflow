package com.qiu.qoj.codesandbox;

import com.qiu.qoj.codesandbox.model.ExecuteCodeRequest;
import com.qiu.qoj.codesandbox.model.ExecuteCodeResponse;

import java.io.IOException;

/**
 * 代码沙箱接口定义
 */
public interface CodeSandbox {

    /**
     * 执行代码
     *
     * @param executeCodeRequest
     * @return
     */
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) throws IOException, InterruptedException;
}
