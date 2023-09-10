package com.julianduru.cdc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Optional;

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


    public Optional<SourceConnector> getConnectorForTopic(String topic) {
        var connectorName = topic.substring(0, topic.indexOf("."));
        return sourceConnectors.stream()
            .filter(connector -> connector.getName().equalsIgnoreCase(connectorName))
            .findFirst();
    }


}

