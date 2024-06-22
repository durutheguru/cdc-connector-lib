package com.julianduru.cdc.bootstrap;

import com.julianduru.cdc.config.ConnectorConfig;
import com.julianduru.cdc.config.ConnectorRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConnectorsBootstrapper {

    private final ConnectorConfig connectorConfig;

    private final List<EngineInstaller> engineInstallers;


    @EventListener(ApplicationStartedEvent.class)
    public void setupConnectors() throws Exception {
        if (!StringUtils.hasText(connectorConfig.getUrl())) {
            log.info("No connector url provided, skipping connector setup");
            return;
        }

        setupSourceConnectors();
        setupSinkConnectors();
    }


    private void setupSourceConnectors() {
        if (connectorConfig.getSourceConnectors() == null || connectorConfig.getSourceConnectors().isEmpty()) {
            log.info("No source connectors to setup");
            return;
        }

        var engine = connectorConfig.getProcessorConfig().getEngine();
        for (var installer : engineInstallers) {
            if (installer.engine() == engine) {
                installer.install(connectorConfig);
            }
        }
    }


    private void setupSinkConnectors() {
        if (connectorConfig.getSinkConnectors() == null || connectorConfig.getSinkConnectors().isEmpty()) {
            log.info("No sink connectors to setup");
            return;
        }

        connectorConfig
            .getSinkConnectors()
            .forEach(
                connector -> installConnector(connectorConfig.getUrl(), connector.request())
            );
    }


    public static void installConnector(String baseUrl, ConnectorRequest request) {
        try {
            log.info("Setting up datasource connector with name {}", request.getName());
            var requestEntity = new HttpEntity<>(request);

            var restTemplateBuilder = new RestTemplateBuilder();
            var template = restTemplateBuilder.build();
            var response = template.exchange(
                baseUrl + "/connectors/", HttpMethod.POST, requestEntity, String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully installed connector with name {}", request.getName());
            } else {
                log.error("Failed to setup datasource connector with name {}", request.getName());
                throw new RuntimeException("Failed to setup datasource connector with name " + request.getName());
            }
        } catch (HttpClientErrorException t) {
            if (t.getStatusCode() == HttpStatus.CONFLICT) {
                log.info("Connector with name {} already exists", request.getName());
            } else {
                throw t;
            }
        }
    }


}


