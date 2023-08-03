package com.julianduru.cdc.config;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.config.SaslConfigs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CdcSaslConfiguration {

    @Value("${queue.config.kafka.security.protocol:}")
    private String securityProtocol;

    @Value("${queue.config.kafka.sasl.jaas.config:}")
    private String saslJaasConfig;

    @Value("${queue.config.kafka.sasl.mechanism:}")
    private String saslMechanism;


    public void configureSasl(Map<String, Object> properties) {
        if ("SASL_SSL".equals(securityProtocol)) {
            if (StringUtils.isEmpty(saslJaasConfig)) {
                throw new IllegalArgumentException("queue.config.kafka.sasl.jaas.config is not set");
            }

            if (StringUtils.isEmpty(saslMechanism)) {
                throw new IllegalArgumentException("queue.config.kafka.sasl.mechanism is not set");
            }

            properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, securityProtocol);
            properties.put(SaslConfigs.SASL_JAAS_CONFIG, saslJaasConfig);
            properties.put(SaslConfigs.SASL_MECHANISM, saslMechanism);
        }
    }


}

