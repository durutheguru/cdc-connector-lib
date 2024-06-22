package com.julianduru.cdc.bootstrap;

import com.julianduru.cdc.config.ConnectorConfig;
import com.julianduru.cdc.config.ProcessorConfig;

/**
 *
 */
public class EmbeddedEngineInstaller implements EngineInstaller {


    @Override
    public void install(ConnectorConfig connectorConfig) {

    }


    @Override
    public ProcessorConfig.Engine engine() {
        return ProcessorConfig.Engine.EMBED;
    }


}
