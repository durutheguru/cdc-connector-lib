package com.julianduru.cdc;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * created by Julian Dumebi Duru on 24/06/2023
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({CdcConnectorLibAutoConfiguration.class})
public @interface EnableCdc {



}
