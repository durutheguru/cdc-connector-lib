package com.julianduru.cdc.processing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;

/**
 *
 */
@Slf4j
@RequiredArgsConstructor
public class RedisLockingMechanism<T> implements LockingMechanism<T> {


    private final RedissonClient redissonClient;


    @Override
    public void lock(LockObject<T> object) {
        var hash = object.getHashable().hash();
        var lock = redissonClient.getLock(hash);

        if (lock.tryLock()) {
            object.getConsumer().accept(object.getMessageRecord());
        }
        else {
            log.info("Skipping message with hash: {} as it has already been processed", hash);
        }
    }


}
