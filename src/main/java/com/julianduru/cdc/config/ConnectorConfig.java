package com.julianduru.cdc.config;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * created by Julian Duru on 28/04/2023
 */
@Data
@Validated
@ConfigurationProperties(prefix = "code.config.connector")
public class ConnectorConfig {


    @NotEmpty(message = "Connector Config url should not be empty")
    private String url;


    @NotEmpty(message = "Connectors should not be empty")
    private List<Connector> connectors;


}
