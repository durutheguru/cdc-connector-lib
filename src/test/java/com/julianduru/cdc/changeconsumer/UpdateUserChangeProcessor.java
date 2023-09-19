package com.julianduru.cdc.changeconsumer;


import com.julianduru.cdc.DataCaptureMap;
import com.julianduru.cdc.annotation.ChangeConsumer;
import com.julianduru.cdc.data.ChangeType;
import com.julianduru.cdc.data.OperationStatus;
import com.julianduru.cdc.data.Payload;
import com.julianduru.cdc.util.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * created by Julian Duru on 01/05/2023
 */
@Slf4j
@RequiredArgsConstructor
@ChangeConsumer(sourceId = "employee.user", changeType = ChangeType.UPDATE)
public class UpdateUserChangeProcessor {

    private final AtomicInteger failureCount = new AtomicInteger(0);


    private final DataCaptureMap dataCaptureMap;


    public OperationStatus process(String reference, Payload payload) {
        log.info("Payload: {}", JSONUtil.asJsonString(payload, ""));

        if (failureCount.getAndIncrement() < 3) {
            return OperationStatus.failure();
        }
        else {
            dataCaptureMap.put(reference, payload);
            failureCount.set(0);
            return OperationStatus.success();
        }
    }


}

