package com.julianduru.cdc;

/**
 *
 */
public abstract class BaseContextIntegrationTest extends BaseServiceIntegrationTest {


    public void peek() throws Exception {
        if (System.getenv("PEEK_MODE").equalsIgnoreCase("enable")) {
            Thread.currentThread().join();
        }
    }

}
