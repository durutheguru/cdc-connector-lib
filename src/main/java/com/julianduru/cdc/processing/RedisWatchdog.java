package com.julianduru.cdc.processing;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
@RequiredArgsConstructor
public class RedisWatchdog {


    private final RedissonClient redissonClient;



    public void watch() {
        /**
         * add isProcessing AtomicBoolean
         * acquire idempotent time factored lock
         * iterate through keys, confirm they are not marked as processed
         * lock key, process key, mark key as processed
         * clear isProcessing atomic boolean
         */
//        var keys = redissonClient.getKeys().getKeysByPattern("cdc:*");
//        while (keys.iterator().hasNext()) {
    }




}

