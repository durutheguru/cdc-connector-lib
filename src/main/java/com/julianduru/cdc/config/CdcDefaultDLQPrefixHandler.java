package com.julianduru.cdc.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * created by Julian Duru on 23/05/2023
 */
@Component
public class CdcDefaultDLQPrefixHandler implements CdcDlqPrefixHandler {


    @Value("${queue.config.consumers.default-group-id}")
    private String groupId;


    @Override
    public String getDLQTopicPrefix() {
        return String.format(
            "%s.%s", CdcDlqPrefixHandler.DLQ_TOPIC_PREFIX_ROOT, groupId
        );
    }


    @Override
    public String addDLQPrefix(String topic) {
        return String.format(
            "%s.%s", getDLQTopicPrefix(), topic
        );
    }

    @Override
    public String[] addDLQPrefix(String[] topics) {
        String[] prefixedTopics = new String[topics.length];
        for (int i = 0; i < topics.length; i++) {
            prefixedTopics[i] = addDLQPrefix(topics[i]);
        }
        return prefixedTopics;
    }


}
