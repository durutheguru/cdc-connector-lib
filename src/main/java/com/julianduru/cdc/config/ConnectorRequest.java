package com.julianduru.cdc.config;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Map;

/**
 * created by Julian Dumebi Duru on 07/06/2023
 */
@Data
@RequiredArgsConstructor
public class ConnectorRequest {

    protected final String name;

    protected final Map<String, String> config;

}
