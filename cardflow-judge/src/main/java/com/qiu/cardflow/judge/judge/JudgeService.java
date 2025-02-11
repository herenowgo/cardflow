package com.qiu.cardflow.judge.judge;

/**
 * 判题服务
 */
public interface JudgeService {

    /**
     * 判题
     *
     * @param questionSubmitIdAndRequestId 使用','分隔的questionSubmitId和requestId
     * @return
     */
    Boolean doJudge(String questionSubmitIdAndRequestId);
}
