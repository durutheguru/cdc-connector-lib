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
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.config.MethodKafkaListenerEndpoint;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Collectors;

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


    private void setupSourceConnectors() throws Exception {
        if (connectorConfig.getSourceConnectors() == null || connectorConfig.getSourceConnectors().isEmpty()) {
            log.info("No source connectors to setup");
            return;
        }

        List<SourceConnectorRequest> requests = connectorConfig.getSourceConnectors()
            .stream()
            .map(SourceConnectorRequest::new)
            .collect(Collectors.toList());

        for (SourceConnectorRequest request : requests) {
            installConnector(request);
            setupCdcTopicConsumers(request);
        }
    }


    private void setupSinkConnectors() throws Exception {
        if (connectorConfig.getSinkConnectors() == null || connectorConfig.getSinkConnectors().isEmpty()) {
            log.info("No sink connectors to setup");
            return;
        }

        List<SinkConnectorRequest> requests = connectorConfig.getSinkConnectors()
            .stream()
            .map(SinkConnectorRequest::new)
            .toList();

        for (SinkConnectorRequest request : requests) {
            installConnector(request);
        }
    }


    private void installConnector(ConnectorRequest request) throws Exception {
        try {
            log.info("Setting up datasource connector with name {}", request.getName());
            HttpEntity<ConnectorRequest> requestEntity = new HttpEntity<>(request);

            RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
            RestTemplate template = restTemplateBuilder.build();
            ResponseEntity<String> response = template.exchange(
                connectorConfig.getUrl() + "/connectors/", HttpMethod.POST, requestEntity, String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully installed connector with name {}", request.getName());
            }
            else {
                log.error("Failed to setup datasource connector with name {}", request.getName());
                throw new Exception("Failed to setup datasource connector with name " + request.getName());
            }
        }
        catch (HttpClientErrorException t) {
            if (t.getStatusCode() == HttpStatus.CONFLICT) {
                log.info("Connector with name {} already exists", request.getName());
            }
            else {
                throw t;
            }
        }
    }


    private void setupCdcTopicConsumers(SourceConnectorRequest request) throws Exception {
        if (request.getSourceConnector().isDisableDefaultConsumer()) {
            log.info("Consumer Disabled for connector with name {}", request.getName());
            return;
        }

        SourceConnector sourceConnector = request.getSourceConnector();
        List<String> tableIncludeList = sourceConnector.getTableIncludeList();

        String[] topics = new String[tableIncludeList.size()];
        for (int i = 0; i < topics.length; i++) {
            topics[i] = request.getName() + "." + tableIncludeList.get(i);
        }

        cdcTopicFactory.createTopics(topics);
        createConsumer(cdcConsumer, topics);

        String[] dlqTopics = dlqPrefixHandler.addDLQPrefix(topics);
        cdcTopicFactory.createTopics(dlqTopics);
        createConsumer(cdcDlqConsumer, dlqTopics);
    }


    private void createConsumer(Consumer consumer, String... topics) throws Exception {
        log.debug("Creating consumer for topic: {}", String.join(", ", topics));

        MethodKafkaListenerEndpoint<String, String> endpoint = new MethodKafkaListenerEndpoint<>();

        endpoint.setId(UUID.randomUUID().toString());
        endpoint.setGroupId(groupId);
        endpoint.setBean(consumer);
        endpoint.setTopics(topics);
        endpoint.setMessageHandlerMethodFactory(new DefaultMessageHandlerMethodFactory());
        endpoint.setMethod(consumer.getClass().getMethod("consume", ConsumerRecord.class));

        Properties consumerProperties = new Properties();
        consumerProperties.putAll(kafkaProperties.buildConsumerProperties());
        endpoint.setConsumerProperties(consumerProperties);

        registry.registerListenerContainer(endpoint, cdcKafkaListenerContainerFactory, true);
    }


}


