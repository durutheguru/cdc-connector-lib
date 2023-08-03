package com.julianduru.cdc.config;

import lombok.Data;

import java.util.Map;

/**
 * created by Julian Dumebi Duru on 07/06/2023
 */
@Data
public class ConnectorRequest {

    protected String name;

    protected Map<String, String> config;

}
