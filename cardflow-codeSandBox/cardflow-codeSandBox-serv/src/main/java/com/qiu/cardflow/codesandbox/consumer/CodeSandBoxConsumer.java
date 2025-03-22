package com.qiu.cardflow.codesandbox.consumer;

import com.qiu.cardflow.codesandbox.controller.ExecuteCodeController;
import com.qiu.cardflow.codesandbox.message.CodeSandBoxToEventStreamMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CodeSandBoxConsumer {

    private final ExecuteCodeController executeCodeController;

    @RabbitListener(queues = "codesandboxToEventStream.queue")
    public void receiveMessage(CodeSandBoxToEventStreamMessage codeSandBoxToEventStreamMessage) {
//        runCodeController
//        // 处理接收到的消息
//        System.out.println("Received message: " + message);
    }
}
