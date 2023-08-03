package com.julianduru.cdc.config;

import com.julianduru.cdc.CdcDlqProducerRecordFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Headers;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.lang.Nullable;

/**
 * created by Julian Duru on 23/05/2023
 */
@Slf4j
public class DLQPublisher extends DeadLetterPublishingRecoverer {


    private final String groupId;

    private final CdcDlqProducerRecordFactory dlqProducerRecordFactory;


    public DLQPublisher(DLQPublisherProperties properties) {
        super(
            properties.getKafkaIntegrationTemplate(),

            (record, exception) -> new TopicPartition (
                record.topic().startsWith(properties.getPrefixHandler().getDLQTopicPrefix()) ? record.topic() :
                    properties.getPrefixHandler().addDLQPrefix(record.topic()),

                -1
            ) // TODO: rethink this, we may want the retried message written to the same partition and consumed by the same consumer
        );

        this.groupId = properties.getGroupId();
        this.dlqProducerRecordFactory = properties.getDlqProducerRecordFactory();
    }


    @Override
    public void accept(ConsumerRecord<?, ?> record, @Nullable Consumer<?, ?> consumer, Exception exception) {
        log.debug("DLQPublisher: Message Record received: " + record);
        dlqProducerRecordFactory.prepare(record, consumer, exception);
        super.accept(record, consumer, exception);
    }


    @Override
    protected ProducerRecord<Object, Object> createProducerRecord(
        ConsumerRecord<?, ?> record, TopicPartition topicPartition, Headers headers,
        @Nullable byte[] key, @Nullable byte[] value
    ) {
        try {
            return dlqProducerRecordFactory.createProducerRecord(record, topicPartition, headers, key, value);
        }
        catch (Throwable t) {
            // TODO: test this scenario
            throw new RuntimeException(t);
        }
    }


}


