package com.qiu.cardflow.codesandbox.strategy;

import com.qiu.cardflow.codesandbox.constant.ProgrammingLanguage;
import com.qiu.cardflow.codesandbox.model.ExecuteCodeRequest;
import com.qiu.cardflow.codesandbox.model.ExecuteCodeResponse;
import com.qiu.cardflow.codesandbox.pool.ContainerInstance;
import com.qiu.cardflow.codesandbox.pool.ContainerPool;
import com.qiu.cardflow.codesandbox.pool.ContainerPoolFactory;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;


@Component
@Slf4j
public abstract class AbstractCodeExecuteStrategyTemplate implements CodeExecuteStrategy {
    @Resource
    private ContainerPoolFactory containerPoolFactory;

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) throws Exception {
        ExecuteCodeResponse executeCodeResponse = null;
        ProgrammingLanguage programmingLanguage = ProgrammingLanguage.fromCode(executeCodeRequest.getLanguage());
        ContainerPool containerPool = containerPoolFactory.getContainerPool(programmingLanguage);
        ContainerInstance containerInstance = null;
        String fileNameInContainer = null;
        try {
            containerInstance = containerPool.borrowContainer();
            fileNameInContainer = buildFileNameInContainer();
            saveCodeToContainer(containerInstance, executeCodeRequest.getCode(), fileNameInContainer);
            executeCodeResponse = runCode(containerInstance, fileNameInContainer, executeCodeRequest.getInputList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (containerInstance != null) {
                containerPool.returnContainer(containerInstance);
            }
            if (fileNameInContainer != null) {
                deleteFileInContainer(containerInstance, fileNameInContainer);
            }
        }

        return executeCodeResponse;
    }

    protected String getFileParentDirectoryInContainer() {
        return "/app";
    }

    private void deleteFileInContainer(ContainerInstance containerInstance, String fileNameInContainer) {
        try {
            containerInstance.execCommand("/bin/sh", "-c", "rm -rf " + getFileParentDirectoryInContainer() + "/*");
        } catch (Exception e) {
            log.error("清理容器文件失败", e);
        }
    }

    protected abstract String buildFileNameInContainer();

    protected abstract ExecuteCodeResponse runCode(ContainerInstance containerInstance, String filePathInContainer, @NotNull List<String> inputList);

    protected void saveCodeToContainer(ContainerInstance containerInstance, String code, String fileNameInContainer) {
        writeStringToFileInContainer(containerInstance, fileNameInContainer, code);
    }


    /**
     * 将字符串写入到容器指定路径的文件中 (如果文件不存在则创建)
     *
     * @param containerInstance   目标容器实例
     * @param fileNameInContainer 容器内的文件路径 (例如: /app/config.txt)
     * @param content             要写入的字符串内容
     */
    protected void writeStringToFileInContainer(ContainerInstance containerInstance, String fileNameInContainer, String content) {
        try {
            try (InputStream tarStream = createTarArchiveInputStreamFromString(fileNameInContainer, content)) {
                containerInstance.getDockerClient().copyArchiveToContainerCmd(containerInstance.getContainerId())
                        .withTarInputStream(tarStream)
                        .withRemotePath("/app") //  根目录，需要完整的路径才能创建文件
                        .exec();
            }
        } catch (IOException e) {
            throw new RuntimeException("将字符串写入容器文件失败", e);
        }
    }


    /**
     * 创建包含字符串内容的 TAR 输入流
     *
     * @param filePathInContainer 容器内的文件路径
     * @param content             要写入的字符串内容
     * @return TAR 输入流
     * @throws IOException 创建 TAR 失败时抛出异常
     */
    private InputStream createTarArchiveInputStreamFromString(String filePathInContainer, String content) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (TarArchiveOutputStream tar = new TarArchiveOutputStream(bos)) {
            tar.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);


            TarArchiveEntry entry = new TarArchiveEntry(filePathInContainer); // 使用完整路径
            byte[] contentBytes = content.getBytes();
            entry.setSize(contentBytes.length);
            tar.putArchiveEntry(entry);
            tar.write(contentBytes);
            tar.closeArchiveEntry();
        }
        return new ByteArrayInputStream(bos.toByteArray());
    }
}
