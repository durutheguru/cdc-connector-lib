package com.julianduru.cdc;

import com.julianduru.cdc.config.ConnectorConfig;
import com.julianduru.cdc.data.OperationStatus;
import com.julianduru.cdc.data.Payload;
import com.julianduru.cdc.processing.KafkaEventHandler;
import com.julianduru.cdc.processing.KafkaRecordProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * created by Julian Duru on 01/05/2023
 */
@Slf4j
@Component
public class CdcProcessor extends KafkaRecordProcessor<Payload> {

    private final CdcProcessorDelegateContainer cdcProcessorDelegateContainer;


    protected CdcProcessor(
        ConnectorConfig connectorConfig,
        KafkaEventHandler<Payload> kafkaEventHandler,
        CdcProcessorDelegateContainer cdcProcessorDelegateContainer
    ) {
        super(Payload.class, connectorConfig.getProcessorConfig(), kafkaEventHandler);
        this.cdcProcessorDelegateContainer = cdcProcessorDelegateContainer;
    }


    public void process(Payload payload) {
        var reference = payload.hash();
        var response = cdcProcessorDelegateContainer.query(reference, payload);
        log.debug("Querying Message Reference: {}. Response: {}", reference, response);

        if (response.getStatus().isTryable()) {
            cdcProcessorDelegateContainer.process(reference, payload);
        }
    }


}


