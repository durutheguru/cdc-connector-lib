package com.julianduru.cdc.bootstrap;

import com.julianduru.cdc.KafkaConsumer;
import com.julianduru.cdc.Consumer;
import com.julianduru.cdc.config.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.config.MethodKafkaListenerEndpoint;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Arrays;
import java.util.Properties;
import java.util.UUID;

/**
 *
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEngineInstaller implements EngineInstaller {

    @Value("${queue.config.consumers.default-group-id}")
    private String groupId;

    private final CdcTopicFactory cdcTopicFactory;

    private final KafkaProperties kafkaProperties;

    private final KafkaListenerEndpointRegistry registry;

    private final KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> cdcKafkaListenerContainerFactory;

    private final KafkaConsumer kafkaConsumer;


    @Override
    public void install(ConnectorConfig connectorConfig) {
        setupSourceConnectors(connectorConfig);
    }


    @Override
    public ProcessorConfig.Engine engine() {
        return ProcessorConfig.Engine.KAFKA;
    }


    private void setupSourceConnectors(ConnectorConfig connectorConfig) {
        connectorConfig
            .getSourceConnectors()
            .forEach(
                connector -> {
                    ConnectorsBootstrapper.installConnector(connectorConfig.getUrl(), connector.request());
                    setupCdcTopicConsumers(connector, connectorConfig.getProcessorConfig());
                }
            );
    }


    private void setupCdcTopicConsumers(SourceConnector connector, ProcessorConfig processorConfig) {
        if (connector.isDisableDefaultConsumer()) {
            log.info("Consumer Disabled for connector with name {}", connector.getName());
            return;
        }

        var tableIncludeList = Arrays.stream(
            connector
                .getConfig()
                .get("table.include.list")
                .split("\\s*,\\s*")
        ).toList();

        String[] topics = new String[tableIncludeList.size()];
        for (int i = 0; i < topics.length; i++) {
            topics[i] = connector.getName() + "." + tableIncludeList.get(i);
        }

        cdcTopicFactory.createTopics(topics);
        createConsumer(kafkaConsumer, processorConfig, topics);
    }


    private void createConsumer(Consumer consumer, ProcessorConfig processorConfig, String... topics) {
        try {
            log.info("Creating consumer for topic: {}", String.join(", ", topics));

            MethodKafkaListenerEndpoint<String, String> endpoint = new MethodKafkaListenerEndpoint<>();

            endpoint.setId(UUID.randomUUID().toString());
            endpoint.setGroupId(groupId);
            endpoint.setBean(consumer);
            endpoint.setTopics(topics);
            endpoint.setMessageHandlerMethodFactory(new DefaultMessageHandlerMethodFactory());
            endpoint.setMethod(consumer.getClass().getMethod("consume", ConsumerRecord.class));
            endpoint.setBatchListener(processorConfig.isBatch());

            Properties consumerProperties = new Properties();
            consumerProperties.putAll(kafkaProperties.buildConsumerProperties(null));
            endpoint.setConsumerProperties(consumerProperties);

            registry.registerListenerContainer(endpoint, cdcKafkaListenerContainerFactory, true);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }


}

