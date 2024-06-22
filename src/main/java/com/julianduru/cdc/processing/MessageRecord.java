package com.julianduru.cdc.processing;

/**
 *
 */
public record MessageRecord<T>(
    int attempts,
    String status,
    T object
) { }
