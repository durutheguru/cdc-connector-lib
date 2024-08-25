package com.julianduru.cdc;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.List;

/**
 * created by Julian Dumebi Duru on 25/05/2023
 */
public interface Consumer {


    void consume(ConsumerRecord<String, String> record);

    void consumeList(List<ConsumerRecord<String, String>> records);


}
