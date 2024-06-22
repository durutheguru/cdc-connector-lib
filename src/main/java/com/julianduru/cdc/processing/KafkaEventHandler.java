package com.julianduru.cdc.processing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.julianduru.cdc.config.ConnectorConfig;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 *
 */
@Component
@RequiredArgsConstructor
public class KafkaEventHandler<T> implements EventHandler<T>{

    protected ObjectMapper jsonMapper = new ObjectMapper();

    private Integer maxRetries;

    private String retryTopic;

    private String dlTopic;

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final ConnectorConfig connectorConfig;


    @PostConstruct
    public void init() {
        var processorConfig = connectorConfig.getProcessorConfig();

        maxRetries = processorConfig.getMaxRetries();
        retryTopic = processorConfig.getRetryTopic();
        dlTopic = processorConfig.getDlTopic();
    }


    @Override
    public void handleSuccess(MessageRecord<T> successfulRecord) {

    }

    @Override
    public void handleFailure(MessageRecord<T> failedRecord) {
        kafkaTemplate.send(composeFailedProducerRecord(failedRecord));
    }


    private Header header(String key, String value) {
        return new Header() {
            @Override
            public String key() {
                return key;
            }

            @Override
            public byte[] value() {
                return StringUtils.hasText(value) ? value.getBytes(StandardCharsets.UTF_8) : new byte[0];
            }
        };
    }


    private ProducerRecord<String, String> composeFailedProducerRecord(MessageRecord<T> messageRecord) {
        try {
            var destinationTopic = "%s".formatted(messageRecord.attempts() < maxRetries ? retryTopic : dlTopic);
            return new ProducerRecord<>(
                destinationTopic, null, (String) null, jsonMapper.writeValueAsString(messageRecord.object()),
                () -> List.of(
                    header(
                        RecordProcessor.MESSAGE_HEADER_PROCESSING_COUNT,
                        messageRecord.attempts() + ""
                    ),
                    header(
                        RecordProcessor.MESSAGE_HEADER_PROCESSING_STATUS,
                        messageRecord.status()
                    )
                ).iterator()
            );
        }
        catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }



}
