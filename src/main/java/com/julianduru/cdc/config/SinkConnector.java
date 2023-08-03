package com.julianduru.cdc.config;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * created by Julian Dumebi Duru on 07/06/2023
 */
@Data
public class SinkConnector {


    private String name;


    @NotNull(message = "Connector Config connector type should not be empty")
    private ConnectorType connectorType;


    private String topics;


    private String url;


    private String username;


    private String password;


    private String insertMode = "upsert";


    private String deleteEnabled = "false";


    private String pkMode = "record_key";


    private String schemaEvolution = "basic";



    public Map<String, String> generateConfigMap() {
        Map<String, String> map = new HashMap<>();

        map.put("connector.class", connectorType.getConnectorClass());
        map.put("tasks.max", "1");
        map.put("topics", topics);
        map.put("connection.url", url);
        map.put("connection.username", username);
        map.put("connection.password", password);
        map.put("insert.mode", insertMode);
        map.put("delete.enabled", deleteEnabled);
        map.put("primary.key.mode", pkMode);
        map.put("schema.evolution", schemaEvolution);
        map.put("database.time_zone", "UTC");

        return map;
    }



}
