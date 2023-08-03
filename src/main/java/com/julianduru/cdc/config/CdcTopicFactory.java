package com.julianduru.cdc.config;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.errors.TopicExistsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * created by Julian Duru on 20/03/2023
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
    prefix = "queue.config.consumers",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class CdcTopicFactory {

    private final KafkaProperties kafkaProperties;

    private final CdcSaslConfiguration cdcSaslConfiguration;

    @Value("${queue.config.topics.partitions:5}")
    private Integer partitions;

    @Value("${queue.config.topics.replication:1}")
    private Integer replication;


    private AdminClient adminClient;


    @PostConstruct
    public void initialize() {
        Map<String, Object> adminClientConfig = new HashMap<>();

        adminClientConfig.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());

        cdcSaslConfiguration.configureSasl(adminClientConfig);

        adminClient = AdminClient.create(
            adminClientConfig
        );
    }


    public void createTopics(String...topics) throws ExecutionException, InterruptedException {
        for (String topic : topics) {
            createTopic(topic);
        }
    }


    public void createTopic(String topic) throws ExecutionException, InterruptedException {
        log.debug("Creating topic: {}", topic); //TODO: partitions and replication should be configurable per connector..
        List<NewTopic> topics = Collections.singletonList(new NewTopic(topic, partitions, replication.shortValue()));
        createTopics(topics);
    }


    public void createTopics(List<NewTopic> topics) throws ExecutionException, InterruptedException {
        try {
            adminClient.createTopics(topics).all().get();
        }
        catch (ExecutionException e) {
            if (e.getCause() instanceof TopicExistsException) {
                log.debug("Topic already exists: {}", e.getMessage());
            }
            else {
                throw e;
            }
        }
    }

}
