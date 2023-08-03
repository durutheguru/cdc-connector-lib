package com.julianduru.cdc.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;

/**
 * created by Julian Duru on 27/02/2023
 */
@TestConfiguration
@EnableAutoConfiguration
public class TestConfig {


    public static boolean testContainersEnabled() {
        String enabled = System.getenv("TEST_CONTAINERS_ENABLED");
        return !"false".equalsIgnoreCase(enabled);
    }


}
