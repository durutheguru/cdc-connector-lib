package com.julianduru.cdc;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * created by Julian Duru on 01/05/2023
 */
@Getter
@RequiredArgsConstructor
public enum ChangeType {


    CREATE("c"),

    UPDATE("u"),

    DELETE("d");


    private final String value;


}

