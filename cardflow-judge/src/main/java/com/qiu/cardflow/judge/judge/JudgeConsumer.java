package com.qiu.cardflow.judge.judge;

import com.qiu.cardflow.judge.message.JudgeAMQPConfig;
import com.qiu.cardflow.judge.message.JudgeDeadLetterConfig;
import com.rabbitmq.client.Channel;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class JudgeConsumer {

    @Resource
    private final JudgeService judgeService;

    @RabbitListener(queues = JudgeAMQPConfig.JUDGE_QUEUE, ackMode = "MANUAL")
    public void onMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag)
            throws IOException {
        // 处理消息
        try {
            judgeService.doJudge(message);
            // 处理成功，确认消息
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            channel.basicNack(deliveryTag, false, false);
            log.error("[JudgeConsumer] judge error, message to dead letter queue", e);
            throw new RuntimeException(e);
        }
    }

    @RabbitListener(queues = JudgeDeadLetterConfig.DEAD_LETTER_QUEUE, ackMode = "MANUAL")
    public void onDeadLetterMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag)
            throws IOException {
        try {
            log.info("[JudgeDeadLetterConsumer] 接收到死信队列消息: {}", message);
            // 分析失败原因并记录
            // 根据失败类型进行补偿处理
            judgeService.doJudge(message);
            // 确认消息处理完成
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("[JudgeDeadLetterConsumer] 处理死信队列消息异常", e);

            // 如果死信队列的消息处理也失败了，通常不应再次nack
            // 而是记录日志，确认消息，并通过其他方式告警
            channel.basicAck(deliveryTag, false);

            // 发送告警通知
        }
    }
}
