package com.julianduru.cdc.bootstrap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.julianduru.cdc.config.ConnectorConfig;
import com.julianduru.cdc.config.ProcessorConfig;
import com.julianduru.cdc.data.Payload;
import com.julianduru.cdc.processing.EmbeddedRecordProcessor;
import com.julianduru.cdc.processing.MessageRecord;
import io.debezium.embedded.EmbeddedEngineChangeEvent;
import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.format.Json;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 *
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmbeddedEngineInstaller implements EngineInstaller {

    protected final ObjectMapper jsonMapper = new ObjectMapper();

    private final EmbeddedRecordProcessor processor;


    @Override
    public void install(ConnectorConfig connectorConfig) {
        setupSourceConnectors(connectorConfig);
    }


    private void setupSourceConnectors(ConnectorConfig connectorConfig) {
        if (connectorConfig.getSourceConnectors() == null || connectorConfig.getSourceConnectors().isEmpty()) {
            log.info("No source connectors to setup");
            return;
        }

        connectorConfig
            .getSourceConnectors()
            .forEach(
                connector -> {
                    var props = new Properties();
                    props.put("name", connector.getName());
                    props.putAll(connector.getConfig());

                    try (DebeziumEngine<ChangeEvent<String, String>> engine = createEngine(props)) {
                        engine.run();
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                    }
                    finally {
                        log.info("Re-initializing connectors");
                        setupSourceConnectors(connectorConfig);
                    }
                }
            );
    }


    private DebeziumEngine<ChangeEvent<String, String>> createEngine(Properties props) {
        return DebeziumEngine.create(Json.class)
            .using(props)
            .notifying(event -> {
                try {
                    processor.process(
                        List.of(
                            new MessageRecord<>(
                                0, "", ((EmbeddedEngineChangeEvent)event).sourceRecord().topic(), jsonMapper.readValue(event.value(), Payload.class)
                            )
                        )
                    );
                }
                catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            })
            .build();
    }


    @Override
    public ProcessorConfig.Engine engine() {
        return ProcessorConfig.Engine.EMBED;
    }


}
