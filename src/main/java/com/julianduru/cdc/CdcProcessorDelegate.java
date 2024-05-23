package com.julianduru.cdc;

import com.julianduru.cdc.data.ChangeType;
import com.julianduru.cdc.data.OperationStatus;
import com.julianduru.cdc.data.Payload;

import java.lang.reflect.Method;
import java.util.function.BiPredicate;

/**
 * created by Julian Duru on 01/05/2023
 */
public interface CdcProcessorDelegate {


    BiPredicate<CdcProcessorDelegate, Payload> DEFAULT_SUPPORTS_PAYLOAD_PREDICATE =
        (delegate, payload) -> payload.sourceId().equalsIgnoreCase(delegate.sourceId()) &&
            payload.getOp().equalsIgnoreCase(delegate.type().getValue());


    String sourceId();


    ChangeType type();


    boolean supports(Payload payload);


    OperationStatus query(String reference, Payload payload);


    OperationStatus process(String reference, Payload payload);



}


