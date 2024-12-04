package com.qiu.qoj.codesandbox.controller;

import com.qiu.qoj.codesandbox.JavaDockerCodeSandboxTemplate;
import com.qiu.qoj.codesandbox.JavaNativeCodeSandbox;
import com.qiu.qoj.codesandbox.model.ExecuteCodeRequest;
import com.qiu.qoj.codesandbox.model.ExecuteCodeResponse;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController("/")
public class MainController {

    // 定义鉴权请求头和密钥
    private static final String AUTH_REQUEST_HEADER = "auth";

    private static final String AUTH_REQUEST_SECRET = "secretKey";

    @Resource
    private JavaDockerCodeSandboxTemplate javaDockerCodeSandboxTemplate;

    @Resource
    private JavaNativeCodeSandbox javaNativeCodeSandbox;


    /**
     * 执行代码
     *
     * @param executeCodeRequest
     * @return
     */
    @PostMapping("/executeCode")
    ExecuteCodeResponse executeCode(@RequestBody ExecuteCodeRequest executeCodeRequest, HttpServletRequest request,
                                    HttpServletResponse response) throws IOException, InterruptedException {
//        // 基本的认证
//        String authHeader = request.getHeader(AUTH_REQUEST_HEADER);
//        if (!AUTH_REQUEST_SECRET.equals(authHeader)) {
//            response.setStatus(403);
//            return null;
//        }
        if (executeCodeRequest == null) {
            throw new RuntimeException("请求参数为空");
        }
        return javaDockerCodeSandboxTemplate.executeCode(executeCodeRequest);
    }
}
