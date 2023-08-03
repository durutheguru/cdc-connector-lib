package com.julianduru.cdc.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * created by Julian Duru on 28/04/2023
 */
@Getter
@RequiredArgsConstructor
public enum ConnectorType {


    MYSQL_SOURCE("io.debezium.connector.mysql.MySqlConnector"),

    JDBC_SINK("io.debezium.connector.jdbc.JdbcSinkConnector");


    private final String connectorClass;


}

