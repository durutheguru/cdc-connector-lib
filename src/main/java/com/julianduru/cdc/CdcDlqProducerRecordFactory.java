package com.julianduru.cdc;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Headers;

/**
 * created by Julian Duru on 24/05/2023
 */
public interface CdcDlqProducerRecordFactory {


    default void prepare(
        ConsumerRecord<?, ?> record,
        Consumer<?, ?> consumer,
        Exception exception
    ) {}


    ProducerRecord<Object, Object> createProducerRecord(
        ConsumerRecord<?, ?> record,
        TopicPartition topicPartition,
        Headers headers,
        byte[] key,
        byte[] value
    ) throws Exception;


}

