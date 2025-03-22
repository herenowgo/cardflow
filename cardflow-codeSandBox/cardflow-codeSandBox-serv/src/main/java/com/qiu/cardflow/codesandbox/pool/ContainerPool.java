package com.qiu.cardflow.codesandbox.pool;

import com.qiu.cardflow.codesandbox.constant.ProgrammingLanguage;

public interface ContainerPool {

    ProgrammingLanguage getProgrammingLanguage();

    /**
     * 借用一个容器(需要保证容器中有/app目录)
     *
     * @return
     * @throws Exception
     */
    public abstract ContainerInstance borrowContainer() throws Exception;

    /**
     * 归还一个容器
     *
     * @param container
     */
    public abstract void returnContainer(ContainerInstance container);
}
