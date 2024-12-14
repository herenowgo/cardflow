package com.qiu.qoj.ai.utils;

import cn.hutool.core.util.RandomUtil;

public class AIUtils {

    public static String generateGroupId() {
        long first = System.currentTimeMillis() % 10000;
        String last = RandomUtil.randomNumbers(4);
        return first + last;
    }
}
