package com.julianduru.cdc.config;

import lombok.Data;

/**
 *
 */
@Data
public class Processing {

    private boolean batch;

    private boolean concurrency;

    private Integer threadPoolSize;

    private Sync sync;

    private Engine engine;


    enum Sync {

        REDIS, RAFT,

    }

    enum Engine {

        EMBED, KAFKA,

    }


}
