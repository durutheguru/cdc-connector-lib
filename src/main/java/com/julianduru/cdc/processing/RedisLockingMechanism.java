package com.julianduru.cdc.processing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

/**
 *
 */
@Slf4j
@RequiredArgsConstructor
public class RedisLockingMechanism<T> implements LockingMechanism<T> {


    private final RedissonClient redissonClient;

    private final Integer lockTimeoutInSeconds;


    @Override
    public void lock(LockObject<T> object) {
        try {
            var hash = object.getHashable().hash();
            initMark(hash);

            var lock = redissonClient.getLock(hash);
            if (
                lock.tryLock(-1, lockTimeoutInSeconds, TimeUnit.SECONDS) &&
                    !isMarked(hash)
            ) {
                object.getConsumer().accept(object.getRecord());
                mark(hash);
            } else {
                log.info("Skipping message with hash: {}. Locked", hash);
            }
        }
        catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }


    private void initMark(String hash) {
        var map = redissonClient.getMap(hash);
        map.putIfAbsent("hash", false);
    }


    private void mark(String hash) {
        var map = redissonClient.getMap(hash);
        map.put("hash", true);
    }


    private boolean isMarked(String hash) {
        var map = redissonClient.getMap(hash);
        return (Boolean) map.get("hash");
    }


}
