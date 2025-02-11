package com.qiu.cardflow.ai.judge;


import com.qiu.cardflow.ai.judge.codesandbox.model.ExecuteCodeResponse;
import com.qiu.cardflow.ai.judge.codesandbox.model.JudgeInfo;
import com.qiu.cardflow.ai.judge.strategy.DefaultJudgeStrategy;
import com.qiu.cardflow.ai.judge.strategy.JavaLanguageJudgeStrategy;
import com.qiu.cardflow.ai.judge.strategy.JudgeContext;
import com.qiu.cardflow.ai.judge.strategy.JudgeStrategy;
import com.qiu.cardflow.ai.model.entity.QuestionSubmit;
import org.springframework.stereotype.Service;

/**
 * 判题管理（简化调用）
 */
@Service
public class JudgeManager {


    /**
     * 执行判题
     *
     * @param judgeContext
     * @param executeCodeResponse
     * @return
     */
    JudgeInfo doJudge(JudgeContext judgeContext, ExecuteCodeResponse executeCodeResponse) {
        QuestionSubmit questionSubmit = judgeContext.getQuestionSubmit();
        String language = questionSubmit.getLanguage();
        JudgeStrategy judgeStrategy = new DefaultJudgeStrategy();
        if ("java".equals(language)) {
            judgeStrategy = new JavaLanguageJudgeStrategy();
        }
        return judgeStrategy.doJudge(judgeContext, executeCodeResponse);
    }

}
