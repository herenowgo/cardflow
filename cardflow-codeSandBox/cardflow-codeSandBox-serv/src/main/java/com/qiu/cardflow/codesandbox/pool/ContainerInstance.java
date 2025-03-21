package com.qiu.cardflow.codesandbox.pool;


import com.github.dockerjava.api.DockerClient;
import lombok.Data;

@Data
public class ContainerInstance {
    private final String containerId;

    private final DockerClient dockerClient;

}