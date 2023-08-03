package com.julianduru.cdc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * created by Julian Duru on 28/04/2023
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "code.config.connector")
public class ConnectorConfig {


    private String url;


    private List<SourceConnector> sourceConnectors;


    private List<SinkConnector> sinkConnectors;


}
