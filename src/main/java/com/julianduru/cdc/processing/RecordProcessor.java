package com.julianduru.cdc.processing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.julianduru.cdc.config.ProcessorConfig;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.StreamSupport;

/**
 *
 */
public abstract class RecordProcessor<T> {

    protected static final String MESSAGE_HEADER_PROCESSING_COUNT = "processing-count";
    protected static final String MESSAGE_HEADER_PROCESSING_STATUS = "processing-status";

    protected ObjectMapper jsonMapper = new ObjectMapper();

    protected Logger log = LoggerFactory.getILoggerFactory().getLogger(this.getClass().getName());

    private ExecutorService executor;

    private final ProcessorConfig config;

    protected final Class<T> typeClass;

    private final EventHandler<T> eventHandler;


    protected RecordProcessor(
        Class<T> typeClass,
        ProcessorConfig config,
        EventHandler<T> eventHandler
    ) {
        this.config = config.valid();
        this.typeClass = typeClass;
        this.eventHandler = eventHandler;

        processConfig();
    }


    private void processConfig() {
        if (config.isConcurrency()) {
            executor = Executors.newFixedThreadPool(config.getThreadPoolSize());
        }
    }


    public Pair<Integer, Integer> process(List<MessageRecord<T>> records) {
        try {
            if (config.isSingleConcurrent()) {
                return processAsync(records);
            }

            if (config.isConcurrency()) {
                return processConcurrent(records);
            } else {
                return processSequential(records);
            }
        }
        catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }


    private Pair<Integer, Integer> processSequential(List<MessageRecord<T>> records) {
        List<MessageRecord<T>> successList = new ArrayList<>();
        List<MessageRecord<T>> failedList = new ArrayList<>();

        for (MessageRecord<T> messageRecord : records) {
            doProcessing(messageRecord, successList, failedList);
        }

        return Pair.of(successList.size(), failedList.size());
    }



    private Pair<Integer, Integer> processConcurrent(List<MessageRecord<T>> records) throws ExecutionException, InterruptedException {
        List<MessageRecord<T>> successList = new ArrayList<>();
        List<MessageRecord<T>> failedList = new ArrayList<>();
        List<Future<?>> submitted = new ArrayList<>();

        for (MessageRecord<T> messageRecord : records) {
            submitted.add(executor.submit(() -> doProcessing(messageRecord, successList, failedList)));
        }

        for (Future<?> f : submitted) {
            f.get();
        }

        return Pair.of(successList.size(), failedList.size());
    }


    private Pair<Integer, Integer> processAsync(List<MessageRecord<T>> records) {
        List<MessageRecord<T>> successList = new ArrayList<>();
        List<MessageRecord<T>> failedList = new ArrayList<>();

        for (MessageRecord<T> messageRecord : records) {
            executor.submit(() -> doProcessing(messageRecord, successList, failedList));
        }

        return Pair.of(-1, -1);
    }


    private void doProcessing(MessageRecord<T> messageRecord, List<MessageRecord<T>> successList, List<MessageRecord<T>> failedList) {
        try {
            process(messageRecord.object());

            eventHandler.handleSuccess(messageRecord);
            successList.add(messageRecord);
        } catch (Exception t) {
            log.error(t.getMessage(), t);

            eventHandler.handleFailure(new MessageRecord<>(messageRecord.attempts() + 1, t.getMessage(), messageRecord.object()));
            failedList.add(messageRecord);
        }
    }


    protected abstract void process(T data);


}

