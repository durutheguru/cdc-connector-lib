package com.julianduru.cdc;

import com.github.javafaker.Faker;

import com.julianduru.cdc.config.ConnectorConfig;
import com.julianduru.cdc.config.TestConfig;
import com.julianduru.cdc.docker.TestDockerComposeContainer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * created by Julian Duru on 27/02/2023
 */
@Slf4j
@ExtendWith({SpringExtension.class})
@SpringBootTest(
    classes = {
        TestConfig.class
    }
)
public abstract class BaseServiceIntegrationTest {

    @Autowired
    private ConnectorConfig connectorConfig;

    protected Faker faker = new Faker();

    protected static TestDockerComposeContainer dockerComposeContainer = new TestDockerComposeContainer();


    static {
        dockerComposeContainer.start();
    }


    @DynamicPropertySource
    protected static void setProperties(DynamicPropertyRegistry registry) {
        if (dockerComposeContainer.isEnabled()) {
            setDatasourceProperties(registry);
            setKafkaProperties(registry);
        }
    }


    private static void setDatasourceProperties(DynamicPropertyRegistry registry) {
        String dbHost = dockerComposeContainer.getServiceHost("mysqldb_1", 33080);

        registry.add(
            "spring.datasource.url",
            () -> String.format(
                "jdbc:mysql://%s:%d/employee?createDatabaseIfNotExist=true&serverTimezone=UTC",
                dbHost, 33080
            )
        );
        registry.add("spring.datasource.username", () -> "root");
        registry.add("spring.datasource.password", () -> "1234567890");
    }


    private static void setKafkaProperties(DynamicPropertyRegistry registry) {
        String kafkaHost = dockerComposeContainer.getServiceHost("kafka_1", 9093);
        String bootstrapServer = String.format("%s:%d", kafkaHost, 9093);
        log.info("Kafka bootstrap server: {}", bootstrapServer);

        registry.add("spring.kafka.bootstrap-servers", () -> bootstrapServer);
    }


}


