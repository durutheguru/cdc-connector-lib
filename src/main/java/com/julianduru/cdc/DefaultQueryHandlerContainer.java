package com.julianduru.cdc;

import com.julianduru.cdc.data.OperationStatus;
import com.julianduru.cdc.data.Payload;
import org.springframework.stereotype.Component;

/**
 * created by Julian Dumebi Duru on 11/09/2023
 */
@Component
public class DefaultQueryHandlerContainer {


    public OperationStatus defaultQueryHandler(String reference, Payload payload) {
        return OperationStatus.pending();
    }


}
