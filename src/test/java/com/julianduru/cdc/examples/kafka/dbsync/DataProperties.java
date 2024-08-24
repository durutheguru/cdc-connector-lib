package com.julianduru.cdc.examples.kafka.dbsync;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 *
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "code.config.data")
public class DataProperties {

    private Source source;

    private Sink sink;


    static class Source extends DataSourceProperties {}

    static class Sink extends DataSourceProperties {}

}

