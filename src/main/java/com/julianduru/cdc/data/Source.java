package com.julianduru.cdc.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * created by Julian Duru on 25/04/2023
 */
@Data
public class Source {


    private String version;


    private String connector;


    private String name;


    @JsonProperty("ts_ms")
    private String timestamp;


    private String snapshot;


    private String db;


    private String table;


    @JsonProperty("server_id")
    private Long serverId;


    private String file;


    private String pos;


}
