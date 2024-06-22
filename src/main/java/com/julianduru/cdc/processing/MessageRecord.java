package com.julianduru.cdc.processing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.UUID;
import java.util.stream.StreamSupport;

/**
 *
 */
@Slf4j
public record MessageRecord<T>(int attempts, String status, T object) implements Hashable {

    private static final ObjectMapper jsonMapper = new ObjectMapper();

    public MessageRecord(int attempts, String status, T object) {
        this.attempts = attempts;
        this.status = status;
        this.object = object;
    }

    public static <T> MessageRecord<T> mapRecord(ConsumerRecord<String, String> consumerRecord, Class<T> typeClass) {
        try {
            T object = jsonMapper.readValue(consumerRecord.value(), typeClass);
            log.debug("Mapped Object from Consumer Record: {}", object);

            var processingCount = StreamSupport.stream(
                    consumerRecord.headers()
                        .headers(RecordProcessor.MESSAGE_HEADER_PROCESSING_COUNT)
                        .spliterator(), false
                )
                .toList();
            var processingStatus = StreamSupport.stream(
                    consumerRecord.headers()
                        .headers(RecordProcessor.MESSAGE_HEADER_PROCESSING_STATUS)
                        .spliterator(), false
                )
                .toList();

            return new MessageRecord<T>(
                processingCount.isEmpty() ? 0 : Integer.parseInt(new String(processingCount.getFirst().value())),
                processingStatus.isEmpty() ? "" : new String(processingStatus.getFirst().value()),
                object
            );
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }


    @Override
    public String hash() {
        //TODO: replace UUID
        return (object instanceof Hashable hashable) ? hashable.hash() : UUID.randomUUID().toString();
    }


}

