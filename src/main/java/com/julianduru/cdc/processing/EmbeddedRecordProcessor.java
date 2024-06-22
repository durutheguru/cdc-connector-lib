package com.julianduru.cdc.processing;

import com.julianduru.cdc.CdcProcessorDelegateContainer;
import com.julianduru.cdc.config.ConnectorConfig;
import com.julianduru.cdc.config.ProcessorConfig;
import com.julianduru.cdc.data.Payload;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
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
        CdcProcessorDelegateContainer cdcProcessorDelegateContainer,
        RedissonClient redissonClient
    ) {
        super(
            Payload.class,
            connectorConfig.getProcessorConfig(),
            eventHandler,
            getLockingMechanism(connectorConfig.getProcessorConfig(), redissonClient)
        );
        this.cdcProcessorDelegateContainer = cdcProcessorDelegateContainer;
    }


    @Override
    protected void process(Payload payload) {
        cdcProcessorDelegateContainer.doHandle(payload);
    }


    private static LockingMechanism<Payload> getLockingMechanism(ProcessorConfig processorConfig, RedissonClient redissonClient) {
        switch (processorConfig.getSync()) {
            case REDIS:
                return new RedisLockingMechanism<>(redissonClient);
            case RAFT:
            default:
                log.warn("Unknown Sync Mechanism: {}", processorConfig.getSync());
        }

        return new NoOpLockingMechanism<>();
    }


}
