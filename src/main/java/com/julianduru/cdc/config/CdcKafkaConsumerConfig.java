package com.julianduru.cdc.config;

import com.julianduru.cdc.CdcDlqProducerRecordFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.*;
import org.springframework.util.backoff.FixedBackOff;

import java.util.Map;

/**
 * created by Julian Duru on 24/02/2023
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(
    prefix = "queue.config.consumers",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class CdcKafkaConsumerConfig {


    private final KafkaProperties kafkaProperties;
    private final CdcSaslConfiguration cdcSaslConfiguration;
    private final CdcDlqPrefixHandler dlqPrefixHandler;
    private final CdcDlqProducerRecordFactory dlqProducerRecordFactory;
    private final KafkaTemplate<String, String> cdcKafkaIntegrationTemplate;

    @Value("${queue.config.consumers.default-group-id}")
    private String groupId;



    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>>
    cdcKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD); // TODO: investigate AckMode options
        factory.setCommonErrorHandler(errorHandler());

        return factory;
    }


    public ConsumerFactory<String, String> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }


    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = kafkaProperties.buildConsumerProperties();
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        cdcSaslConfiguration.configureSasl(props);

        return props;
    }


    public DefaultErrorHandler errorHandler() {
        DLQPublisher[] recoverer = new DLQPublisher[1];

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
            recoverer[0] = publishingRecoverer(),
            new FixedBackOff(0L, 0L)
        ); // TODO: Back-Off should be configurable

        errorHandler.setRetryListeners(
            (record, ex, deliveryAttempt) -> {
                log.error(
                    String.format(
                        "Failed Record in Retry Listener. Exception : %s, deliveryAttempt: %s",
                        ex.getMessage(), deliveryAttempt
                    ), ex
                );
                recoverer[0].accept(record, ex);
            }
        );

        return errorHandler;
    }


    public DLQPublisher publishingRecoverer() {
        return new DLQPublisher(
            DLQPublisherProperties.builder()
                .groupId(groupId)
                .kafkaIntegrationTemplate(cdcKafkaIntegrationTemplate)
                .dlqProducerRecordFactory(dlqProducerRecordFactory)
                .prefixHandler(dlqPrefixHandler)
                .build()
        );
    }


}


