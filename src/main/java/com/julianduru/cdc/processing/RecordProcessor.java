package com.julianduru.cdc.processing;

import com.julianduru.cdc.config.ProcessorConfig;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *
 */
public abstract class RecordProcessor<T extends Hashable> {

    protected static final String MESSAGE_HEADER_PROCESSING_COUNT = "processing-count";
    protected static final String MESSAGE_HEADER_PROCESSING_STATUS = "processing-status";

    protected Logger log = LoggerFactory.getILoggerFactory().getLogger(this.getClass().getName());

    private ExecutorService executor;

    private final ProcessorConfig config;

    protected final Class<T> typeClass;

    private final EventHandler<T> eventHandler;

    private final LockingMechanism<T> lockingMechanism;


    protected RecordProcessor(
        Class<T> typeClass,
        ProcessorConfig config,
        EventHandler<T> eventHandler,
        LockingMechanism<T> lockingMechanism
    ) {
        this.config = config.valid();
        this.typeClass = typeClass;
        this.eventHandler = eventHandler;
        this.lockingMechanism = lockingMechanism;

        processConfig();
    }


    private void processConfig() {
        if (config.isConcurrent()) {
            executor = Executors.newFixedThreadPool(config.getThreadPoolSize());
        }
    }


    public Pair<Integer, Integer> process(List<MessageRecord<T>> records) {
        try {
            if (config.isSingleConcurrent()) {
                processAsync(records);
                return Pair.of(records.size(), 0);
            }
            else if (config.isConcurrent()) {
                return processConcurrent(records);
            }
            else {
                return processSerial(records);
            }
        }
        catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }


    private Pair<Integer, Integer> processSerial(List<MessageRecord<T>> records) {
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


    private void processAsync(List<MessageRecord<T>> records) {
        for (MessageRecord<T> messageRecord : records) {
            executor.submit(() -> doProcessing(messageRecord, null, null));
        }
    }


    private void doProcessing(MessageRecord<T> messageRecord, List<MessageRecord<T>> successList, List<MessageRecord<T>> failedList) {
        lockingMechanism.lock(
            LockObject.<T>builder()
                .hashable(messageRecord)
                .record(messageRecord)
                .consumer(
                    record -> {
                        try {
                            process(record.object());
                            eventHandler.handleSuccess(messageRecord);
                            if (successList != null) {
                                successList.add(messageRecord);
                            }
                        } catch (Exception t) {
                            log.error(t.getMessage(), t);
                            eventHandler.handleFailure(new MessageRecord<>(messageRecord.attempts() + 1, t.getMessage(), messageRecord.topic(), messageRecord.object()));
                            if (failedList != null) {
                                failedList.add(messageRecord);
                            }
                        }
                    }
                )
                .build()
        );
    }


    protected abstract void process(T data);


}

