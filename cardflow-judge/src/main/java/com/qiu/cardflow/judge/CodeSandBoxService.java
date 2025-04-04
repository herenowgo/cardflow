package com.qiu.cardflow.judge;

import com.qiu.cardflow.judge.judge.codesandbox.model.ExecuteCodeRequest;
import com.qiu.cardflow.judge.judge.codesandbox.model.ExecuteCodeResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;

@FeignClient("cardflow-codeSandBox")
public interface CodeSandBoxService {

    /**
     * 执行代码
     *
     * @param executeCodeRequest
     * @return
     */
    @PostMapping("/api/executeCode")
    ExecuteCodeResponse executeCode(@RequestBody ExecuteCodeRequest executeCodeRequest) throws IOException, InterruptedException;
}

