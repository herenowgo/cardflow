package com.qiu.cardflow.codesandbox.pool;

import cn.hutool.core.io.FileUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Volume;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
@Component

public class JavaContainerPool implements ContainerPool {

    @Resource
    private DockerClient dockerClient;

    private GenericObjectPool<ContainerInstance> containerPool;

    // 使用ConcurrentHashMap存储容器实例和其挂载目录的映射
    private final ConcurrentHashMap<String, String> containerToHostDirectoryMap = new ConcurrentHashMap<>();

    private static final String IMAGE_NAME = "openjdk:8-alpine";

    private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";

    @PostConstruct
    private void init() throws Exception {
        // 检查镜像是否存在，不存在则拉取
        try {
            boolean imageExists = dockerClient.listImagesCmd()
                    .withImageNameFilter(IMAGE_NAME)
                    .exec().stream()
                    .anyMatch(image -> image.getRepoTags() != null &&
                            Arrays.asList(image.getRepoTags()).contains(IMAGE_NAME));

            if (!imageExists) {
                log.info("镜像 {} 不存在，开始拉取...", IMAGE_NAME);
                dockerClient.pullImageCmd(IMAGE_NAME)
                        .start()
                        .awaitCompletion();
                log.info("镜像 {} 拉取完成", IMAGE_NAME);
            } else {
                log.info("镜像 {} 已存在", IMAGE_NAME);
            }
        } catch (Exception e) {
            log.error("检查或拉取镜像失败: {}", IMAGE_NAME, e);
            throw e;
        }

        GenericObjectPoolConfig<ContainerInstance> config = new GenericObjectPoolConfig<>();
        config.setMaxTotal(3); // 最大容器数
        config.setMaxIdle(3);   // 最大空闲容器数
        config.setMinIdle(2);   // 最小空闲容器数
        config.setTestOnBorrow(true); // 借用容器时测试是否可用
        config.setTestOnReturn(true); // 归还容器时测试是否可用

        containerPool = new GenericObjectPool<>(new DockerContainerFactory(), config);

        // 预热容器池
        for (int i = 0; i < config.getMinIdle(); i++) {
            containerPool.addObject();
        }

        log.info("Docker容器池初始化完成，预热容器数: {}", config.getMinIdle());
    }

    @Override
    public ContainerInstance borrowContainer() throws Exception {
        ContainerInstance containerInstance = containerPool.borrowObject();
//        setHostDirectory(containerInstance.getContainerId(), hostDirectory);
        return containerInstance;
    }

    @Override
    public void returnContainer(ContainerInstance container) {
        containerPool.returnObject(container);
    }

    private void invalidateContainer(ContainerInstance container) {
        try {
            containerPool.invalidateObject(container);
        } catch (Exception e) {
            log.error("无效化容器失败: {}", container.getContainerId(), e);
        }
    }

    @PreDestroy
    private void destroy() throws Exception {
        containerPool.close();
        log.info("Docker容器池已关闭");
    }

    private void setHostDirectory(String containerId, String hostDirectory) {
        containerToHostDirectoryMap.put(containerId, hostDirectory);


    }

    private String getHostDirectory(String containerId) {
        return containerToHostDirectoryMap.get(containerId);
    }

    private void removeHostDirectory(String containerId) {
        containerToHostDirectoryMap.remove(containerId);
    }

    private class DockerContainerFactory extends BasePooledObjectFactory<ContainerInstance> {

        @Override
        public ContainerInstance create() throws Exception {
            // 创建容器
            CreateContainerCmd containerCmd = dockerClient.createContainerCmd(IMAGE_NAME);
            HostConfig hostConfig = new HostConfig();
            hostConfig.withMemory(50 * 1000 * 1000L);
            hostConfig.withMemorySwap(0L);
            hostConfig.withCpuCount(1L);

            String userDir = System.getProperty("user.dir");
            String globalCodePathName = userDir + File.separator + GLOBAL_CODE_DIR_NAME;
            // 判断全局代码目录是否存在，没有则新建
            if (!FileUtil.exist(globalCodePathName)) {
                FileUtil.mkdir(globalCodePathName);
            }
            hostConfig.setBinds(new Bind(globalCodePathName, new Volume("/app")));
            CreateContainerResponse createContainerResponse = containerCmd
                    .withHostConfig(hostConfig)
                    .withNetworkDisabled(true)
                    .withReadonlyRootfs(true)
                    .withAttachStdin(true)
                    .withAttachStderr(true)
                    .withAttachStdout(true)
                    .withTty(true)
                    .exec();

            String containerId = createContainerResponse.getId();

            // 启动容器
            dockerClient.startContainerCmd(containerId).exec();
            log.info("创建并启动了新容器: {}", containerId);

            return new ContainerInstance(containerId, dockerClient);
        }

        @Override
        public PooledObject<ContainerInstance> wrap(ContainerInstance container) {
            return new DefaultPooledObject<>(container);
        }

        @Override
        public void destroyObject(PooledObject<ContainerInstance> p) throws Exception {
            ContainerInstance container = p.getObject();
            String containerId = container.getContainerId();

            log.info("销毁容器: {}", containerId);
            // 停止并删除容器
            dockerClient.stopContainerCmd(containerId).exec();
            dockerClient.removeContainerCmd(containerId).exec();

            // 移除挂载路径记录
            removeHostDirectory(containerId);
        }

        @Override
        public boolean validateObject(PooledObject<ContainerInstance> p) {
            ContainerInstance container = p.getObject();
            try {
                InspectContainerResponse response = dockerClient.inspectContainerCmd(container.getContainerId()).exec();
                return response.getState().getRunning();
            } catch (Exception e) {
                log.error("容器验证失败: {}", container.getContainerId(), e);
                return false;
            }
        }
    }
}
