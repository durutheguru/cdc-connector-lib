package com.julianduru.cdc.examples;


import com.julianduru.cdc.BaseServiceIntegrationTest;
import com.julianduru.cdc.CdcConnectorLibAutoConfiguration;
import com.julianduru.cdc.config.TestConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * created by Julian Dumebi Duru on 11/06/2023
 */
@Slf4j
@SpringBootTest(
    classes = {
        TestConfig.class,
        CdcConnectorLibAutoConfiguration.class
    }
)
@EnableAutoConfiguration
@ActiveProfiles("db-sync")
public class DatabaseSyncTest extends BaseServiceIntegrationTest {



    @Test
    void dbSyncTest() throws Exception {
        if ("true".equalsIgnoreCase(System.getenv("INSPECTION_ENABLED"))) {
            Thread.currentThread().join();
        }

        //TODO: write validations against destination db
    }


}
