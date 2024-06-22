package com.julianduru.cdc;

import com.julianduru.cdc.data.Payload;
import com.julianduru.cdc.processing.MessageRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * created by Julian Duru on 29/04/2023
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaConsumer implements Consumer {


    private final KafkaEngineProcessor kafkaEngineProcessor;



    @Override
    public void consume(List<ConsumerRecord<String, String>> records) {
        try {
            var topic = records.getFirst().topic();
            log.info("CdcConsumer - Received {} messages from topic: {}", records.size(), topic);
            var processed = kafkaEngineProcessor.process(
                records.stream().map(r -> MessageRecord.mapRecord(r, Payload.class)).toList()
            );
            log.info("CdcConsumer - Processed Topic: {}, Successful {}, Failed {}", topic, processed.getLeft(), processed.getRight());
        }
        catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }


}

