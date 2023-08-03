package com.julianduru.cdc.config;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * created by Julian Duru on 28/04/2023
 */
@Data
public class Connector {


    @NotEmpty(message = "Connector Config name should not be empty")
    private String name;


    private DatabaseVendor databaseVendor;


    private String databaseHost;


    private Integer databasePort;


    private String databaseUsername;


    private String databasePassword;


    private List<String> databaseIncludeList;


    private List<String> kafkaBootstrapServers;


}

