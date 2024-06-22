package com.julianduru.cdc.processing;

/**
 *
 */
public interface EventHandler<T> {

    void handleSuccess(MessageRecord<T> successfulRecord);

    void handleFailure(MessageRecord<T> failedRecord);

}
