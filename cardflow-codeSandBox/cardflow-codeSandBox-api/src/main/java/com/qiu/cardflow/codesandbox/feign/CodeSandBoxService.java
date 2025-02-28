package com.qiu.cardflow.codesandbox.feign;


import com.qiu.cardflow.codesandbox.dto.ExecuteCodeRequest;
import com.qiu.cardflow.codesandbox.dto.ExecuteCodeResponse;
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

