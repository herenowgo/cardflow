package com.qiu.cardflow.codesandbox.consumer;

import com.qiu.cardflow.codesandbox.controller.ExecuteCodeController;
import com.qiu.cardflow.codesandbox.message.CodeSandBoxToEventStreamMessage;
import com.qiu.cardflow.codesandbox.model.ExecuteCodeResponse;
import com.qiu.cardflow.codesandbox.service.ExecuteCodeService;
import com.qiu.codeflow.eventStream.dto.EventMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CodeSandBoxConsumer {

    private final ExecuteCodeController executeCodeController;

    private final ExecuteCodeService executeCodeService;

    private final RabbitTemplate rabbitTemplate;

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
        rabbitTemplate.convertAndSend("eventMessage-queue", "eventMessage-queue.dev", eventMessage);
    }
}
