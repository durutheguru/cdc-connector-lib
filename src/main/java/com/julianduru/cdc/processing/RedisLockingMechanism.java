package com.julianduru.cdc.processing;

import com.julianduru.cdc.util.JSON;
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
            initProcessing(hash, object.getRecord());

            var lock = redissonClient.getLock(hash);
            if (
                !isProcessed(hash) && lock.tryLock(-1, lockTimeoutInSeconds, TimeUnit.SECONDS)
            ) {
                object.getConsumer().accept(object.getRecord());
                doneProcessing(hash);
            } else {
                log.info("Skipping message with hash: {}. Marked or Locked", hash);
            }
        }
        catch (InterruptedException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }


    private void initProcessing(String hash, MessageRecord<T> message) {
        var map = redissonClient.getMap(prefixKey(hash));
        map.putIfAbsent("processed", false);
        map.putIfAbsent("data", JSON.stringify(message));
    }


    private void doneProcessing(String hash) {
        var map = redissonClient.getMap(prefixKey(hash));
        map.put("processed", true);
    }


    private boolean isProcessed(String hash) {
        var map = redissonClient.getMap(prefixKey(hash));
        var value = map.get("processed");
        return value != null ? (Boolean) value : false;
    }


    private String prefixKey(String key) {
        return "cdc:%s".formatted(key);
    }


}
