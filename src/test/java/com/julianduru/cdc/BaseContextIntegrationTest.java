package com.julianduru.cdc;

/**
 *
 */
public abstract class BaseContextIntegrationTest extends BaseServiceIntegrationTest {


    public void peek() {
        try {
            if ("enable".equalsIgnoreCase(System.getenv("PEEK_MODE"))) {
                Thread.currentThread().join();
            }
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
