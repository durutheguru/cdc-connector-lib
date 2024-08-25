package com.julianduru.cdc;

import org.junit.jupiter.api.AfterEach;

/**
 *
 */
public abstract class BaseContextIntegrationTest extends BaseServiceIntegrationTest {


    private void peek() {
        try {
            if ("enable".equalsIgnoreCase(System.getenv("PEEK_MODE"))) {
                Thread.currentThread().join();
            }
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    @AfterEach
    public void after() {
        peek();
    }


}
