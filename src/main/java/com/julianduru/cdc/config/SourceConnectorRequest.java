package com.julianduru.cdc.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/**
 * created by Julian Duru on 29/04/2023
 */
@Data
public class SourceConnectorRequest extends ConnectorRequest {

    @JsonIgnore
    private SourceConnector sourceConnector;


    public SourceConnectorRequest(SourceConnector sourceConnector) {
        this.sourceConnector = sourceConnector;
        this.prepare();
    }


    private void prepare() {
        this.name = sourceConnector.getName();
        this.config = sourceConnector.generateConfigMap();
    }


    public SourceConnector getSourceConnector() {
        return sourceConnector;
    }


}

