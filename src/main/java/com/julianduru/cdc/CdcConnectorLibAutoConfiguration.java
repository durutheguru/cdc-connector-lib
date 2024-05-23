package com.julianduru.cdc;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * created by Julian Duru on 29/04/2023
 */
@Configuration
@ComponentScan(basePackages = "com.julianduru.cdc")
@EnableConfigurationProperties
public class CdcConnectorLibAutoConfiguration {



}
