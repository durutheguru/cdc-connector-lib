package com.julianduru.cdc;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

/**
 *
 */
@ActiveProfiles("db-sync")
public class DbSyncIntegrationTest extends BaseContextIntegrationTest {


    @Test
    public void testSynchronizationOfDataFromSourceToSync() throws Exception {
        //TODO: write assertions...

        super.peek();
    }


}

