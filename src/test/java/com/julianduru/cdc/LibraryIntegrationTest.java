package com.julianduru.cdc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 *
 */
@ExtendWith({SpringExtension.class})
@SpringBootTest(
    classes = {
        CdcConnectorLibAutoConfiguration.class,
    }
)
public class LibraryIntegrationTest {


    @Test
    public void contextLoads() throws Exception {
        Thread.currentThread().join();
    }

}
