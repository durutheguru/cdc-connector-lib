package com.julianduru.cdc.processing;

/**
 *
 */
public interface RecordProcessorConfig {

    String getTopic();

    String getRetryTopic();

    String getDltTopic();

    int getPoolSize();

    int getMaxRetries();

}
