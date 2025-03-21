package com.qiu.cardflow.codesandbox;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(scanBasePackages = "com.qiu.cardflow")
public class QojCodeSandboxApplication {

    public static void main(String[] args) {
        SpringApplication.run(QojCodeSandboxApplication.class, args);
    }

    @Bean
    DockerClient getDockerClient() {
        DefaultDockerClientConfig.Builder configBuilder = DefaultDockerClientConfig.createDefaultConfigBuilder();
    
    String osName = System.getProperty("os.name").toLowerCase();
    if (osName.contains("win")) {
        // Windows环境
        configBuilder.withDockerHost("tcp://localhost:2375");
    } else {
        // Linux/Mac环境
        configBuilder.withDockerHost("unix:///var/run/docker.sock");
    }
    
    DockerClientConfig config = configBuilder.build();

    DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
            .dockerHost(config.getDockerHost())
            .sslConfig(config.getSSLConfig())
            .build();

    return DockerClientImpl.getInstance(config, httpClient);
    }
}
