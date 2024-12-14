package work.codeflow.eventStream.util;

import cn.hutool.core.util.RandomUtil;

public class EventMessageUtil {
    public static String generateRequestId() {
        long first = System.currentTimeMillis() % 10000;
        String last = RandomUtil.randomNumbers(4);
        return first + last;
    }
}
