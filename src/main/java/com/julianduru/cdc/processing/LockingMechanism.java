package com.julianduru.cdc.processing;

/**
 *
 */
public interface LockingMechanism<T> {


    void lock(LockObject<T> object);


}

