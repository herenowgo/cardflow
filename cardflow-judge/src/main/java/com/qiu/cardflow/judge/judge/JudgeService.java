package com.qiu.cardflow.judge.judge;

import java.io.IOException;

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
    Boolean doJudge(String questionSubmitIdAndRequestId) throws IOException, InterruptedException;
}
