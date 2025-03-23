package com.qiu.cardflow.codesandbox.consumer;

import com.qiu.cardflow.codesandbox.message.CodeSandBoxToEventStreamMessage;
import com.qiu.cardflow.codesandbox.model.ExecuteCodeResponse;
import com.qiu.cardflow.codesandbox.service.ExecuteCodeService;
import com.qiu.cardflow.redis.starter.key.EventStreamKeyBuilder;
import com.qiu.codeflow.eventStream.dto.EventMessage;
import com.qiu.codeflow.eventStream.message.EventStreamMessageConstant;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CodeSandBoxConsumer {

    private final ExecuteCodeService executeCodeService;

    private final RabbitTemplate rabbitTemplate;

    private final EventStreamKeyBuilder eventStreamKeyBuilder;

    private final RedisTemplate<String, String> redisTemplate;

    @RabbitListener(queues = "codesandboxToEventStream.queue")
    public void receiveMessage(CodeSandBoxToEventStreamMessage codeSandBoxToEventStreamMessage) throws Exception {
        // Validate the message
        if (codeSandBoxToEventStreamMessage == null || codeSandBoxToEventStreamMessage.getExecuteCodeRequest() == null) {
            return;
        }

        ExecuteCodeResponse executeCodeResponse = executeCodeService.executeCode(codeSandBoxToEventStreamMessage.getExecuteCodeRequest());
        EventMessage eventMessage = EventMessage.builder()
                .data(executeCodeResponse)
                .eventType(codeSandBoxToEventStreamMessage.getEventType())
                .requestId(codeSandBoxToEventStreamMessage.getRequestId())
                .userId(codeSandBoxToEventStreamMessage.getUserId())
                .build();
        String userToEventKey = eventStreamKeyBuilder.buildUserToEventStreamKey(codeSandBoxToEventStreamMessage.getUserId());
        String routingKey = redisTemplate.opsForValue().get(userToEventKey);
        rabbitTemplate.convertAndSend(EventStreamMessageConstant.EVENT_STREAM_EXCHANGE, routingKey, eventMessage);
    }
}
