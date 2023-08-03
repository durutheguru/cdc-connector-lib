package com.julianduru.cdc.annotation;

import com.julianduru.cdc.data.ChangeType;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * created by Julian Dumebi Duru on 28/05/2023
 */
@Component
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ChangeConsumer {


    String QUERY_METHOD_NAME = "query";

    String PROCESS_METHOD_NAME = "process";

    String SUPPORTED_PAYLOAD_METHOD_NAME = "supports";


    String sourceId();


    ChangeType changeType();



    enum Type {

        PROCESSOR, QUERY

    }


}
