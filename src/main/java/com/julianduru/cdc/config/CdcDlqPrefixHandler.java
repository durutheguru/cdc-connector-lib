package com.julianduru.cdc.config;

/**
 * created by Julian Duru on 23/05/2023
 */
public interface CdcDlqPrefixHandler {


    String DLQ_TOPIC_PREFIX_ROOT = "dlq";


    String getDLQTopicPrefix();


    String addDLQPrefix(String topic);


    String[] addDLQPrefix(String[] topics);


}
