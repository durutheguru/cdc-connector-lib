package com.julianduru.cdc.changeconsumer;


import com.julianduru.cdc.DataCaptureMap;
import com.julianduru.cdc.annotation.ChangeConsumer;
import com.julianduru.cdc.data.ChangeType;
import com.julianduru.cdc.data.OperationStatus;
import com.julianduru.cdc.data.Payload;
import com.julianduru.cdc.util.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


/**
 * created by Julian Duru on 01/05/2023
 */
@Slf4j
@RequiredArgsConstructor
@ChangeConsumer(sourceId = "employee.user", changeType = ChangeType.CREATE)
public class CreateUserChangeProcessor {


    private final DataCaptureMap dataCaptureMap;


    public OperationStatus process(String reference, Payload payload) {
        log.info("Payload: {}", JSONUtil.asJsonString(payload, ""));
        dataCaptureMap.put(reference, payload);
        return OperationStatus.success();
    }


}


