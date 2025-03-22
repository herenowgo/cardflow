package com.qiu.cardflow.codesandbox.pool;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Volume;
import com.qiu.cardflow.codesandbox.constant.ProgrammingLanguage;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;


@Slf4j
@Component
@ConfigurationProperties(prefix = "docker.pool.java")
public class JavaContainerPool implements ContainerPool {


    @Resource
    private DockerClient dockerClient;

    private GenericObjectPool<ContainerInstance> containerPool;

    private static final String IMAGE_NAME = "openjdk:8-alpine";

    @Setter
    private Integer maxTotal;

    @Setter
    private Integer maxIdle;

    @Setter
    private Integer minIdle;

    @Setter
    private Long cpuCount;

    @Setter
    private Long memoryLimitByte;

    /**
     * docker容器资源是系统的核心资源，所以根据fail-fast原则，在Spring上下文可用之前需要保证必要的容器全部初始化完成
     *
     * @throws Exception
     */
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
        config.setMaxTotal(maxTotal); // 最大容器数
        config.setMaxIdle(maxIdle);   // 最大空闲容器数
        config.setMinIdle(minIdle);   // 最小空闲容器数
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
    public ProgrammingLanguage getProgrammingLanguage() {
        return ProgrammingLanguage.JAVA;
    }

    @Override
    public ContainerInstance borrowContainer() throws Exception {
        return containerPool.borrowObject();
    }

    @Override
    public void returnContainer(ContainerInstance container) {
        containerPool.returnObject(container);
    }

    @PreDestroy
    private void destroy() {
        containerPool.close();
        log.info("Docker容器池已关闭");
    }


    private class DockerContainerFactory extends BasePooledObjectFactory<ContainerInstance> {

        @Override
        public ContainerInstance create() {
            // 创建容器
            CreateContainerCmd containerCmd = dockerClient.createContainerCmd(IMAGE_NAME);
            HostConfig hostConfig = new HostConfig();
            hostConfig.withMemory(memoryLimitByte);
            hostConfig.withMemorySwap(0L);
            hostConfig.withCpuCount(cpuCount);

            // 创建一个匿名卷来保存代码文件
            Volume volume = new Volume("/app");
            CreateContainerResponse createContainerResponse = containerCmd
                    .withHostConfig(hostConfig)
                    .withNetworkDisabled(true)
                    .withReadonlyRootfs(true) // 设置只读根文件系统
                    .withVolumes(volume)// 添加一个匿名卷，这个卷会是可写的
                    .withAttachStdin(true)
                    .withAttachStderr(true)
                    .withAttachStdout(true)
                    .withTty(true)
                    .exec();

            String containerId = createContainerResponse.getId();

            // 启动容器
            dockerClient.startContainerCmd(containerId).exec();

            // 在容器中创建"/app"目录


            log.info("创建并启动了新容器: {}", containerId);
            return new ContainerInstance(containerId, dockerClient);
        }

        @Override
        public PooledObject<ContainerInstance> wrap(ContainerInstance container) {
            return new DefaultPooledObject<>(container);
        }

        @Override
        public void destroyObject(PooledObject<ContainerInstance> p) {
            ContainerInstance container = p.getObject();
            String containerId = container.getContainerId();

            // 停止并删除容器(包括相关的匿名卷)
            dockerClient.stopContainerCmd(containerId).exec();
            dockerClient.removeContainerCmd(containerId)
                    .withRemoveVolumes(true)
                    .exec();

            log.info("销毁容器: {}", containerId);
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
