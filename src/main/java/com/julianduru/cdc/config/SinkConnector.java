package com.julianduru.cdc.config;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Map;

/**
 * created by Julian Dumebi Duru on 07/06/2023
 */
@Data
public class SinkConnector {

    @NotEmpty(message = "Connector Config name should not be empty")
    private String name;

    @NotEmpty(message = "Connector Config should not be empty")
    private Map<String, String> config;


    public ConnectorRequest request() {
        return new ConnectorRequest(getName(), getConfig());
    }


}
