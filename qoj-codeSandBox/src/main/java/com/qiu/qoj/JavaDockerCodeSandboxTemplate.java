package com.qiu.qoj;

import cn.hutool.core.io.FileUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.qiu.qoj.model.ExecuteCodeRequest;
import com.qiu.qoj.model.ExecuteCodeResponse;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class JavaDockerCodeSandboxTemplate implements CodeSandbox {

    private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";

    private static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";


    private final DockerClient dockerClient;

    public JavaDockerCodeSandboxTemplate(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
    }

    /**
     * 执行代码
     *
     * @param executeCodeRequest
     * @return
     */
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) throws IOException, InterruptedException {
        File userCodeFile = null;
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        try {
            userCodeFile = saveCodeToFile(executeCodeRequest.getCode());

            String containerId = startUpOrGetContainer(dockerClient, userCodeFile);

            compileCode(dockerClient, containerId, executeCodeResponse);

            if (!executeCodeResponse.getCompileErrorOutput().isEmpty()) {
                return executeCodeResponse;
            }

            runCode(dockerClient, containerId, executeCodeRequest.getInputList(), 3L, TimeUnit.SECONDS, executeCodeResponse);
            return executeCodeResponse;
        } finally {
            deleteFile(userCodeFile);
        }


//        return null;
    }


    /**
     * 5、删除文件
     *
     * @param userCodeFile
     * @return
     */
    public boolean deleteFile(File userCodeFile) {
        if (userCodeFile.getParentFile() != null) {
            String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
            boolean del = FileUtil.del(userCodeParentPath);
            System.out.println("删除" + (del ? "成功" : "失败"));
            return del;
        }
        return true;
    }

    protected void runCode(DockerClient dockerClient, String containerId, List<String> inputList, long time, TimeUnit timeUnit, ExecuteCodeResponse executeCodeResponse) throws IOException, InterruptedException {

        List<String> runOutput = executeCodeResponse.getRunOutput();
        List<String> runErrorOutput = executeCodeResponse.getRunErrorOutput();
        List<Long> times = executeCodeResponse.getTime();

        for (String input : inputList) {
            try (PipedOutputStream pipedOutputStream = new PipedOutputStream();
                 PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream);
                 ByteArrayOutputStream runOutputStream = new ByteArrayOutputStream();
                 ByteArrayOutputStream runErrorStream = new ByteArrayOutputStream()) {


                ExecStartResultCallback runResultCallback = new ExecStartResultCallback(runOutputStream, runErrorStream);

                // 创建新的 exec 命令
                ExecCreateCmdResponse runCmdResponse = dockerClient.execCreateCmd(containerId)
                        .withCmd("/bin/sh", "-c", "java -cp /app Main")
                        .withAttachStdin(true)
                        .withAttachStdout(true)
                        .withAttachStderr(true)
                        .exec();

                String runExecId = runCmdResponse.getId();

                // 启动执行命令
                long startTime = System.nanoTime();

                dockerClient.execStartCmd(runExecId)
                        .withStdIn(pipedInputStream)
                        .withDetach(false)
                        .withTty(false)
                        .exec(runResultCallback);

                // 写入输入
                pipedOutputStream.write((input + "\n").getBytes());
                pipedOutputStream.flush();

                // 等待执行完成
                runResultCallback.awaitCompletion(time, timeUnit);
                long endTime = System.nanoTime();
                long durationInMillis = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

                runOutputStream.flush();
                runErrorStream.flush();
                // 将当前执行结果加入到 result 列表
                runOutput.add(runOutputStream.toString());
                runErrorOutput.add(runErrorStream.toString());
                times.add(durationInMillis);
            }
        }
    }


    /**
     * 1. 把用户的代码保存为文件
     *
     * @param code 用户代码
     * @return
     */
    public File saveCodeToFile(String code) {
        String userDir = System.getProperty("user.dir");
        String globalCodePathName = userDir + File.separator + GLOBAL_CODE_DIR_NAME;
        // 判断全局代码目录是否存在，没有则新建
        if (!FileUtil.exist(globalCodePathName)) {
            FileUtil.mkdir(globalCodePathName);
        }

        // 把用户的代码隔离存放
        String userCodeParentPath = globalCodePathName + File.separator + UUID.randomUUID();
        String userCodePath = userCodeParentPath + File.separator + GLOBAL_JAVA_CLASS_NAME;
        File userCodeFile = FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);
        return userCodeFile;
    }


    protected String startUpOrGetContainer(DockerClient dockerClient, File userCodeFile) {
        String image = "openjdk:8-alpine";
        dockerClient.inspectImageCmd(image).exec();

        String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
        CreateContainerCmd containerCmd = dockerClient.createContainerCmd(image);
        HostConfig hostConfig = new HostConfig();
        hostConfig.withMemory(100 * 1000 * 1000L);
        hostConfig.withMemorySwap(0L);
        hostConfig.withCpuCount(1L);
//        hostConfig.withSecurityOpts(Arrays.asList("seccomp=安全管理配置字符串"));
        hostConfig.setBinds(new Bind(userCodeParentPath, new Volume("/app")));
        CreateContainerResponse createContainerResponse = containerCmd
                .withHostConfig(hostConfig)
                .withNetworkDisabled(true)
                .withReadonlyRootfs(true)
                .withAttachStdin(true)
                .withAttachStderr(true)
                .withAttachStdout(true)
                .withTty(true)
                .exec();
        System.out.println(createContainerResponse);
        String containerId = createContainerResponse.getId();

        InspectContainerResponse containerInfo = dockerClient.inspectContainerCmd(containerId).exec();
        if (!containerInfo.getState().getRunning()) {
            dockerClient.startContainerCmd(containerId).exec();
        } else {
            System.out.println("Container is already running.");
        }

        return containerId;
    }


    public void compileCode(DockerClient dockerClient, String containerId, ExecuteCodeResponse executeCodeResponse) throws InterruptedException, IOException {
        // Step 1: 执行 Java 编译命令 (javac)
        ExecCreateCmdResponse compileCmdResponse = dockerClient.execCreateCmd(containerId)
                .withCmd("/bin/sh", "-c", "javac /app/Main.java") // 编译 Java 文件
                .withAttachStdin(true)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .exec();

        String compileExecId = compileCmdResponse.getId();

        try (ByteArrayOutputStream compileOutputStream = new ByteArrayOutputStream();
             ByteArrayOutputStream compileErrorStream = new ByteArrayOutputStream()) {
            // 捕获输出到 ByteArrayOutputStream
            ExecStartResultCallback compileResultCallback = new ExecStartResultCallback(compileOutputStream, compileErrorStream);

            // 启动命令
            dockerClient.execStartCmd(compileExecId)
                    .withDetach(false)
                    .withTty(false)
                    .exec(compileResultCallback);

            // 等待命令执行完成
            compileResultCallback.awaitCompletion(3, TimeUnit.SECONDS);

            // 确保输出流已关闭并刷新
            compileOutputStream.flush();
            compileErrorStream.flush();
//            executeCodeResponse.
//            return new ComplieResult(compileOutputStream.toString(), compileErrorStream.toString());
            executeCodeResponse.setCompileOutput(compileOutputStream.toString());
            executeCodeResponse.setCompileErrorOutput(compileErrorStream.toString());
        }
    }


}
