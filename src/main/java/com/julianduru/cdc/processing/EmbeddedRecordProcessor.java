package com.julianduru.cdc.processing;

import com.julianduru.cdc.CdcProcessorDelegateContainer;
import com.julianduru.cdc.config.ConnectorConfig;
import com.julianduru.cdc.config.ProcessorConfig;
import com.julianduru.cdc.data.Payload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 *
 */
@Slf4j
@Component
public class EmbeddedRecordProcessor extends RecordProcessor<Payload> {

    private final CdcProcessorDelegateContainer cdcProcessorDelegateContainer;


    protected EmbeddedRecordProcessor(
        ConnectorConfig connectorConfig,
        EmbeddedEventHandler<Payload> eventHandler,
        CdcProcessorDelegateContainer cdcProcessorDelegateContainer
    ) {
        super(Payload.class, connectorConfig.getProcessorConfig(), eventHandler);
        this.cdcProcessorDelegateContainer = cdcProcessorDelegateContainer;
    }

    @Override
    protected void process(Payload payload) {
        cdcProcessorDelegateContainer.doHandle(payload);
    }

}
