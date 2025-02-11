package com.qiu.cardflow.judge.judge.strategy;

import com.qiu.cardflow.judge.judge.codesandbox.model.JudgeInfo;
import com.qiu.cardflow.judge.model.dto.question.JudgeCase;
import com.qiu.cardflow.judge.model.entity.Question;
import com.qiu.cardflow.judge.model.entity.QuestionSubmit;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 上下文（用于定义在策略中传递的参数）
 */
@Data
@Builder
public class JudgeContext {

    private JudgeInfo judgeInfo;

    private List<String> inputList;

    private List<String> outputList;

    private List<JudgeCase> judgeCaseList;

    private Question question;

    private QuestionSubmit questionSubmit;

}
