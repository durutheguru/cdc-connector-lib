package com.julianduru.cdc.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/**
 * created by Julian Dumebi Duru on 07/06/2023
 */
@Data
public class SinkConnectorRequest extends ConnectorRequest {


    @JsonIgnore
    private SinkConnector sinkConnector;


    public SinkConnectorRequest(SinkConnector sinkConnector) {
        this.sinkConnector = sinkConnector;
        this.prepare();
    }


    private void prepare() {
        this.name = sinkConnector.getName();
        this.config = sinkConnector.generateConfigMap();
    }


    public SinkConnector getSinkConnector() {
        return sinkConnector;
    }




}
