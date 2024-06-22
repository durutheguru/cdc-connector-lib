package com.julianduru.cdc.config;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 *
 */
@Data
public class ProcessorConfig {

    private boolean batch;

    private boolean concurrent;

    private Integer threadPoolSize;

    @NotNull(message = "Processor config sync is required")
    private Sync sync;

    @NotNull(message = "Processor config engine is required")
    private Engine engine;

    private Integer maxRetries;

    private String retryTopic;

    private String dlTopic;


    public enum Sync {

        REDIS, RAFT,

    }

    public enum Engine {

        EMBED, KAFKA,

    }

    public boolean isSingleConcurrent() {
        return !isBatch() && isConcurrent();
    }


    public ProcessorConfig valid() throws IllegalStateException {
        if (isConcurrent()) {
            if (getThreadPoolSize() == null) {
                throw new IllegalStateException("Thread Pool Size must be set when concurrency is enabled");
            }
            if (getThreadPoolSize() <= 0) {
                throw new IllegalStateException("Thread Pool Size must be greater than 0");
            }
        }

        if (getEngine() == Engine.EMBED && isBatch()) {
            throw new IllegalStateException("Batch processing is not supported for embedded engine");
        }

        if (getEngine() != Engine.EMBED && sync != null) {
            throw new IllegalStateException("Sync is only supported for embedded engine");
        }

        if (getEngine() == Engine.KAFKA) {
            if (retryTopic == null) {
                throw new IllegalStateException("Retry Topic is required for Kafka Engine");
            }
            if (dlTopic == null) {
                throw new IllegalStateException("Dead Letter Topic is required for Kafka Engine");
            }
            if (maxRetries == null) {
                throw new IllegalStateException("Max Retries is required for Kafka Engine");
            }
        }

        return this;
    }


}

