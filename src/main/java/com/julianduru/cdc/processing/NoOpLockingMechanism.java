package com.julianduru.cdc.processing;

/**
 *
 */
public class NoOpLockingMechanism<T> implements LockingMechanism<T> {

    @Override
    public void lock(LockObject<T> object) {
        object
            .getConsumer()
            .accept(object.getRecord());
    }


}
