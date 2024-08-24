package com.julianduru.cdc.examples.kafka;

import com.julianduru.cdc.BaseContextIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

/**
 *
 */
@ActiveProfiles("kafka-single-event")
public class SingleEventConsumerTest extends BaseContextIntegrationTest {


    @Test
    public void testProcessingSingleEventFromSource() {
        super.peek();
    }


}
