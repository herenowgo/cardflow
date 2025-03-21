package com.qiu.cardflow.codesandbox.pool;

public interface ContainerPool {
    /**
     * 借用一个容器
     *
     * @return
     * @throws Exception
     */
    ContainerInstance borrowContainer() throws Exception;

    /**
     * 归还一个容器
     *
     * @param container
     */
    void returnContainer(ContainerInstance container);
}
