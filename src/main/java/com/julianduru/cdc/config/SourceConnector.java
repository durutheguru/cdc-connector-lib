package com.julianduru.cdc.config;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Map;

/**
 * created by Julian Duru on 28/04/2023
 */
@Data
public class SourceConnector {

    @NotEmpty(message = "Connector Config name should not be empty")
    private final String name;

    @NotEmpty(message = "Connector Config should not be empty")
    private final Map<String, String> config;

    private final boolean disableDefaultConsumer;


    public ConnectorRequest request() {
        return new ConnectorRequest(getName(), getConfig());
    }


}


