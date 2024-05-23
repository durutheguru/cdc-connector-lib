package com.julianduru.cdc.changeconsumer;


import com.julianduru.cdc.annotation.ChangeConsumer;
import com.julianduru.cdc.data.ChangeType;
import com.julianduru.cdc.data.OperationStatus;
import com.julianduru.cdc.data.Payload;
import com.julianduru.cdc.util.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor


@ChangeConsumer(sourceId = "employee.user", changeType = ChangeType.CREATE)
public class CreateUserChangeProcessor {


    public OperationStatus process(Payload payload) {
        log.debug("New User inserted: {}", JSON.stringify(payload));
        // handle logic for inserted user
        return OperationStatus.success();
    }


}


