package com.julianduru.cdc.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.julianduru.cdc.util.HashUtil;
import lombok.Data;

import java.util.Arrays;
import java.util.Map;

/**
 * created by Julian Duru on 25/04/2023
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Payload {


    Map<String, Object> before;


    Map<String, Object> after;


    private String op;


    private Source source;


    private String timestamp;


    public String sourceId() {
        return source.getDb() + "." + source.getTable();
    }

    public boolean isCreate() {
        return "c".equals(op);
    }

    public boolean isUpdate() {
        return "u".equals(op);
    }

    public boolean isDelete() {
        return "d".equals(op);
    }


    public String hash() {
        return HashUtil.hashObjectList(
            Arrays.asList(
                before,
                after,
                op,
                source,
                timestamp
            )
        );
    }


}


