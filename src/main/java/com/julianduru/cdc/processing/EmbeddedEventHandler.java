package com.julianduru.cdc.processing;

import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class EmbeddedEventHandler<T> implements EventHandler<T> {

    @Override
    public void handleSuccess(MessageRecord<T> successfulRecord) {

    }


    @Override
    public void handleFailure(MessageRecord<T> failedRecord) {

    }

}
