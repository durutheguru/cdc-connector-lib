package com.julianduru.cdc.bootstrap;

import com.julianduru.cdc.CdcConsumer;
import com.julianduru.cdc.CdcDlqConsumer;
import com.julianduru.cdc.Consumer;
import com.julianduru.cdc.config.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.config.MethodKafkaListenerEndpoint;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Arrays;
import java.util.Properties;
import java.util.UUID;

/**
 * created by Julian Duru on 29/04/2023
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConnectorsBootstrapper {

    @Value("${queue.config.consumers.default-group-id}")
    private String groupId;

    private final ConnectorConfig connectorConfig;

    private final KafkaProperties kafkaProperties;

    private final KafkaListenerEndpointRegistry registry;

    private final CdcDlqPrefixHandler dlqPrefixHandler;

    private final CdcConsumer cdcConsumer;

    private final CdcDlqConsumer cdcDlqConsumer;

    private final CdcTopicFactory cdcTopicFactory;


    @Autowired(required = false)
    private KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> cdcKafkaListenerContainerFactory;


    @EventListener(ApplicationStartedEvent.class)
    public void setupConnectors() throws Exception {
        if (!StringUtils.hasText(connectorConfig.getUrl())) {
            log.info("No connector url provided, skipping connector setup");
            return;
        }

        setupSourceConnectors();
        setupSinkConnectors();
    }


    private void setupSourceConnectors() {
        if (connectorConfig.getSourceConnectors() == null || connectorConfig.getSourceConnectors().isEmpty()) {
            log.info("No source connectors to setup");
            return;
        }

        connectorConfig
            .getSourceConnectors()
            .forEach(
                connector -> {
                    installConnector(connector.request());
                    setupCdcTopicConsumers(connector);
                }
            );
    }


    private void setupSinkConnectors() throws Exception {
        if (connectorConfig.getSinkConnectors() == null || connectorConfig.getSinkConnectors().isEmpty()) {
            log.info("No sink connectors to setup");
            return;
        }

        connectorConfig
            .getSinkConnectors()
            .forEach(
                connector -> {
                    installConnector(connector.request());
                }
            );
    }


    private void installConnector(ConnectorRequest request) throws RuntimeException {
        try {
            log.info("Setting up datasource connector with name {}", request.getName());
            var requestEntity = new HttpEntity<>(request);

            var restTemplateBuilder = new RestTemplateBuilder();
            var template = restTemplateBuilder.build();
            var response = template.exchange(
                connectorConfig.getUrl() + "/connectors/", HttpMethod.POST, requestEntity, String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully installed connector with name {}", request.getName());
            } else {
                log.error("Failed to setup datasource connector with name {}", request.getName());
                throw new RuntimeException("Failed to setup datasource connector with name " + request.getName());
            }
        } catch (HttpClientErrorException t) {
            if (t.getStatusCode() == HttpStatus.CONFLICT) {
                log.info("Connector with name {} already exists", request.getName());
            } else {
                throw t;
            }
        }
    }


    private void setupCdcTopicConsumers(SourceConnector connector) {
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
        createConsumer(cdcConsumer, topics);

        String[] dlqTopics = dlqPrefixHandler.addDLQPrefix(topics);
        cdcTopicFactory.createTopics(dlqTopics);
        createConsumer(cdcDlqConsumer, dlqTopics);
    }


    private void createConsumer(Consumer consumer, String... topics) {
        try {
            log.debug("Creating consumer for topic: {}", String.join(", ", topics));

            MethodKafkaListenerEndpoint<String, String> endpoint = new MethodKafkaListenerEndpoint<>();

            endpoint.setId(UUID.randomUUID().toString());
            endpoint.setGroupId(groupId);
            endpoint.setBean(consumer);
            endpoint.setTopics(topics);
            endpoint.setMessageHandlerMethodFactory(new DefaultMessageHandlerMethodFactory());
            endpoint.setMethod(consumer.getClass().getMethod("consume", ConsumerRecord.class));

            Properties consumerProperties = new Properties();
            consumerProperties.putAll(kafkaProperties.buildConsumerProperties(null));
            endpoint.setConsumerProperties(consumerProperties);

            registry.registerListenerContainer(endpoint, cdcKafkaListenerContainerFactory, true);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }


}


