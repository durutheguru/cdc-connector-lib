package com.julianduru.cdc.config;

import com.moniepoint.cdc.CdcDlqProducerRecordFactory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * created by Julian Duru on 24/05/2023
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DLQPublisherProperties {

    private KafkaTemplate<?, ?> kafkaIntegrationTemplate;

    private CdcDlqPrefixHandler prefixHandler;

    private String groupId;

    private CdcDlqProducerRecordFactory dlqProducerRecordFactory;

}
