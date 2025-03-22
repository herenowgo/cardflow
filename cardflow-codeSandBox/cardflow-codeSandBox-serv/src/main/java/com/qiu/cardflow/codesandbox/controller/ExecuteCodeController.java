package com.qiu.cardflow.codesandbox.controller;

import com.qiu.cardflow.codesandbox.model.ExecuteCodeRequest;
import com.qiu.cardflow.codesandbox.model.ExecuteCodeResponse;
import com.qiu.cardflow.codesandbox.service.ExecuteCodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("/")
@RequiredArgsConstructor
public class ExecuteCodeController {


    private final ExecuteCodeService executeCodeService;

    /**
     * 执行代码
     *
     * @param executeCodeRequest
     * @return
     */
    @PostMapping("/executeCode")
    ExecuteCodeResponse executeCode(@RequestBody @Valid ExecuteCodeRequest executeCodeRequest) throws Exception {
        if (executeCodeRequest == null) {
            throw new RuntimeException("请求参数为空");
        }
        return executeCodeService.executeCode(executeCodeRequest);
    }
}
