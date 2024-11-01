package com.qiu.qoj.judge.strategy;

import com.qiu.qoj.judge.codesandbox.model.ExecuteCodeResponse;
import com.qiu.qoj.judge.codesandbox.model.JudgeInfo;

public interface JudgeStrategy {

    /**
     * 执行判题
     *
     * @param judgeContext
     * @param executeCodeResponse
     * @return
     */
    JudgeInfo doJudge(JudgeContext judgeContext, ExecuteCodeResponse executeCodeResponse);
}
