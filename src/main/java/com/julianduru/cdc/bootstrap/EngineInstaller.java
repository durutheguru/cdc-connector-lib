package com.julianduru.cdc.bootstrap;

import com.julianduru.cdc.config.ConnectorConfig;
import com.julianduru.cdc.config.ProcessorConfig;

/**
 *
 */
public interface EngineInstaller {

    default void doInstall(ConnectorConfig connectorConfig) {
        if (engine() == connectorConfig.getProcessorConfig().getEngine()) {
            install(connectorConfig);
        }
    }

    void install(ConnectorConfig connectorConfig);

    ProcessorConfig.Engine engine();

}
