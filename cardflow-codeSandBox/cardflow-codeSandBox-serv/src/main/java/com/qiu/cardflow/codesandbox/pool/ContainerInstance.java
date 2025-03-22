package com.qiu.cardflow.codesandbox.pool;


import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import lombok.Data;

import java.io.OutputStream;

@Data
public class ContainerInstance {
    private final String containerId;

    private final DockerClient dockerClient;

    public void execCommand(String... command) {
        try {
            ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                    .withCmd(command)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();

            dockerClient.execStartCmd(execCreateCmdResponse.getId())
                    .exec(new ExecStartResultCallback())
                    .awaitCompletion();
        } catch (Exception e) {
            throw new RuntimeException("执行命令失败", e);
        }
    }

    public ExecStartResultCallback execCommandWithOutput(OutputStream stdout, OutputStream stderr, String... command) {
        try {
            ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                    .withCmd(command)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();

            ExecStartResultCallback callback = new ExecStartResultCallback(stdout, stderr);
            dockerClient.execStartCmd(execCreateCmdResponse.getId()).exec(callback);
            return callback;
        } catch (Exception e) {
            throw new RuntimeException("执行命令失败", e);
        }
    }
}