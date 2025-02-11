package com.qiu.cardflow.judge.judge.strategy;

import com.qiu.cardflow.judge.judge.codesandbox.model.ExecuteCodeResponse;
import com.qiu.cardflow.judge.judge.codesandbox.model.JudgeInfo;

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
