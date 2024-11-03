package com.qiu.qoj.judge;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class JudgeConsumer {

    @Resource
    private final JudgeService judgeService;


    @Bean
    public Consumer<Long> questionSubmit() {
        return judgeService::doJudge;
    }
}
