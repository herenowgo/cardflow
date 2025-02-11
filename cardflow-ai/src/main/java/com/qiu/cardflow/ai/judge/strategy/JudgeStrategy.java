package com.qiu.cardflow.ai.judge.strategy;

import com.qiu.cardflow.ai.judge.codesandbox.model.ExecuteCodeResponse;
import com.qiu.cardflow.ai.judge.codesandbox.model.JudgeInfo;

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
