package com.julianduru.cdc.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * created by julian on 24/01/2023
 */
@Configuration
@RequiredArgsConstructor
public class CdcKafkaProducerConfig {


    @Autowired(required = false)
    private CdcTopicFactory cdcTopicFactory;

    private final KafkaProperties kafkaProperties;
    private final List<NewTopic> topics;
    private final CdcSaslConfiguration cdcSaslConfiguration;


    @Bean
    public Map<String, Object> cdcProducerConfig() {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildProducerProperties());

        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        cdcSaslConfiguration.configureSasl(props);

        return props;
    }


    @Bean
    public ProducerFactory<String, String> cdcProducerFactory() {
        return new DefaultKafkaProducerFactory<>(cdcProducerConfig());
    }


    @Bean
    public KafkaTemplate<String, String> cdcKafkaIntegrationTemplate() {
        return new KafkaTemplate<>(cdcProducerFactory());
    }


    @PostConstruct
    public void initialize() throws Exception {
        cdcTopicFactory.createTopics(topics);
    }


}


