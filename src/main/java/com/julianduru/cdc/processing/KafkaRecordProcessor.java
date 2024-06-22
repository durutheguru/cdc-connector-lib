package com.julianduru.cdc.processing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.julianduru.cdc.config.ProcessorConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 *
 */

public abstract class KafkaRecordProcessor<T> extends RecordProcessor<T> {



    protected KafkaRecordProcessor(
        Class<T> typeClass,
        ProcessorConfig processorConfig,
        KafkaEventHandler<T> kafkaEventHandler
    ) {
        super(typeClass, processorConfig, kafkaEventHandler);
    }


}

