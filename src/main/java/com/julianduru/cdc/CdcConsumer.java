package com.julianduru.cdc;

import com.julianduru.cdc.config.ConnectorConfig;
import com.julianduru.cdc.data.CdcMessage;
import com.julianduru.cdc.data.OperationStatus;
import com.julianduru.cdc.data.Payload;
import com.julianduru.cdc.exception.CdcProcessingException;
import com.julianduru.cdc.util.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * created by Julian Duru on 29/04/2023
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CdcConsumer implements Consumer {


    private final CdcProcessor cdcProcessor;



    @Override
    public void consume(List<ConsumerRecord<String, String>> records) {
        try {
            var topic = records.getFirst().topic();
            log.info("CdcConsumer - Received {} messages from topic: {}", records.size(), topic);
            var processed = cdcProcessor.process(records);
            log.info("CdcConsumer - Processed Topic: {}, Successful {}, Failed {}", topic, processed.getLeft(), processed.getRight());
        }
        catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }


}

