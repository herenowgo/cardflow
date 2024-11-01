package com.qiu.qoj.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;


@Configuration
public class DataSourceConfiguration {


    //指定当前对象作为bean
    @Bean("dataSource")
    //指定dataSource来DI
    @Qualifier(value = "dataSource")
    //primary将当前数据库连接池作为默认数据库连接池
    @Primary
    //在application.yml文件中增加的前缀spring.datasource.c3p0
    @ConfigurationProperties(prefix = "spring.datasource.c3p0")
    public DataSource dataSource() {
        DataSource dataSource = DataSourceBuilder.create().type(com.mchange.v2.c3p0.ComboPooledDataSource.class).build();
        return dataSource;
    }

}



