package com.qiu.cardflow.ai.judge;

/**
 * 判题服务
 */
public interface JudgeService {

    /**
     * 判题
     *
     * @param questionSubmitId
     * @return
     */
    Boolean doJudge(long questionSubmitId);
}
