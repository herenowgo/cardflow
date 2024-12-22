package com.qiu.qoj.config;

import org.neo4j.cypherdsl.core.renderer.Dialect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class CypherDslConfiguration {

    @Bean
    @Primary
    org.neo4j.cypherdsl.core.renderer.Configuration cypherDslConfiguration2() {
        return org.neo4j.cypherdsl.core.renderer.Configuration.newConfig()
                .withDialect(Dialect.NEO4J_5).build();
    }

}
