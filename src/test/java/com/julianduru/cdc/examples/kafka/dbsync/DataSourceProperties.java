package com.julianduru.cdc.examples.kafka.dbsync;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 *
 */
@Data
public class DataSourceProperties {

    private String url;

    private String username;

    private String password;

    private String driverClassName;

}
