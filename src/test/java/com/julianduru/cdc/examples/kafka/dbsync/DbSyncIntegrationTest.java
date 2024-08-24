package com.julianduru.cdc.examples.kafka.dbsync;

import com.julianduru.cdc.BaseContextIntegrationTest;
import org.assertj.db.type.Table;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;

/**
 *
 */
@ActiveProfiles("db-sync")
public class DbSyncIntegrationTest extends BaseContextIntegrationTest {

    @Autowired
    private DataSource mysqlDataSource;

    @Autowired
    private DataSource postgresDataSource;


    @Test
    public void testSynchronizationOfDataFromSourceToSync() {
        super.peek();
    }


}

