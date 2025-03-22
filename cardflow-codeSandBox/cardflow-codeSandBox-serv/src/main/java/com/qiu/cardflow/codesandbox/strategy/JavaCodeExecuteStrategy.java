package com.qiu.cardflow.codesandbox.strategy;

import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.qiu.cardflow.codesandbox.constant.ProgrammingLanguage;
import com.qiu.cardflow.codesandbox.model.ExecuteCodeResponse;
import com.qiu.cardflow.codesandbox.pool.ContainerInstance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class JavaCodeExecuteStrategy extends AbstractCodeExecuteStrategyTemplate {

    private static final String CLASS_NAME = "Main";
    private static final String JAVA_FILE_NAME = CLASS_NAME + ".java";
    private static final String JAVA_FILE_PATH = "/app/" + JAVA_FILE_NAME;
    private static final long EXECUTION_TIMEOUT = 3L; // 代码执行超时时间(秒)

    @Override
    protected String buildFileNameInContainer() {
        return JAVA_FILE_NAME;
    }

    @Override
    protected ExecuteCodeResponse runCode(ContainerInstance containerInstance, String filePathInContainer, List<String> inputList) {
        ExecuteCodeResponse response = new ExecuteCodeResponse();

        // 1. 编译代码
        try {
            compileJavaCode(containerInstance, response);

            // 如果有编译错误，直接返回结果
            if (!response.getCompileErrorOutput().isEmpty()) {
                return response;
            }

            // 2. 执行代码
            executeJavaCode(containerInstance, inputList, response);

        } catch (Exception e) {
            log.error("Java代码执行异常", e);
            response.getRunErrorOutput().add("系统错误: " + e.getMessage());
        }

        return response;
    }

    /**
     * 编译Java代码
     */
    private void compileJavaCode(ContainerInstance containerInstance, ExecuteCodeResponse response) throws IOException, InterruptedException {
        ByteArrayOutputStream compileOutputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream compileErrorStream = new ByteArrayOutputStream();

        try {
            // 执行编译命令
            ExecStartResultCallback callback = containerInstance.execCommandWithOutput(
                    compileOutputStream,
                    compileErrorStream,
                    "javac", "-encoding", "UTF-8", "-source", "8", "-target", "8", JAVA_FILE_PATH
            );

            // 等待编译完成
            callback.awaitCompletion(EXECUTION_TIMEOUT, TimeUnit.SECONDS);

            // 设置编译输出
            String compileOutput = compileOutputStream.toString();
            String compileError = compileErrorStream.toString();

            response.setCompileOutput(compileOutput);
            response.setCompileErrorOutput(compileError);

        } finally {
            compileOutputStream.close();
            compileErrorStream.close();
        }
    }

    /**
     * 执行Java代码
     */
    private void executeJavaCode(ContainerInstance containerInstance, List<String> inputList, ExecuteCodeResponse response) {
        List<String> runOutput = new ArrayList<>();
        List<String> runErrorOutput = new ArrayList<>();
        List<Long> executionTimes = new ArrayList<>();

        for (String input : inputList) {
            try (PipedOutputStream pipedOutputStream = new PipedOutputStream();
                 PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream);
                 ByteArrayOutputStream stdout = new ByteArrayOutputStream();
                 ByteArrayOutputStream stderr = new ByteArrayOutputStream()) {

                // 准备输入流

                try {
                    pipedOutputStream.write((input + "\n").getBytes());
                    pipedOutputStream.flush();
                } catch (IOException e) {
                    log.error("写入输入流失败", e);
                }
                // 记录开始时间
                long startTime = System.nanoTime();

                // 执行Java程序
                ExecStartResultCallback callback = containerInstance.getDockerClient()
                        .execStartCmd(containerInstance.getDockerClient().execCreateCmd(containerInstance.getContainerId())
                                .withCmd("java", "-cp", getFileParentDirectoryInContainer(), CLASS_NAME)
                                .withAttachStdin(true)
                                .withAttachStdout(true)
                                .withAttachStderr(true)
                                .exec().getId())
                        .withStdIn(pipedInputStream)
                        .exec(new ExecStartResultCallback(stdout, stderr));

                // 等待执行完成，设置超时时间
                boolean completed = callback.awaitCompletion(EXECUTION_TIMEOUT, TimeUnit.SECONDS);

                // 记录结束时间和执行时长
                long endTime = System.nanoTime();
                long executionTime = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

                // 获取执行结果
                String output = stdout.toString().trim();
                String errorOutput = stderr.toString().trim();

                // 记录执行结果
                runOutput.add(output);
                if (!errorOutput.isEmpty()) {
                    runErrorOutput.add(errorOutput);
                } else if (!completed) {
                    runErrorOutput.add("代码执行超时");
                } else {
                    runErrorOutput.add(""); // 保持输出列表大小一致
                }
                executionTimes.add(executionTime);

            } catch (Exception e) {
                log.error("执行Java代码异常", e);
                runOutput.add("");
                runErrorOutput.add("执行异常: " + e.getMessage());
                executionTimes.add(0L);
            }
        }

        // 设置执行结果
        response.setRunOutput(runOutput);
        response.setRunErrorOutput(runErrorOutput);
        response.setTime(executionTimes);
    }

    @Override
    public ProgrammingLanguage getProgrammingLanguage() {
        return ProgrammingLanguage.JAVA;
    }
}