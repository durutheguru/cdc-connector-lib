package com.julianduru.cdc.docker;

import com.julianduru.cdc.config.TestConfig;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.time.Duration;

/**
 * created by julian on 04/11/2022
 */
public class TestDockerComposeContainer extends DockerComposeContainer {

    private boolean enabled;


    public TestDockerComposeContainer() {
        super(new File("src/main/resources/docker-compose.yml"));

        this.enabled = TestConfig.testContainersEnabled();
        withExposedService(
            "mysqldb_1", 33080,
            Wait.forHealthcheck()
                .withStartupTimeout(Duration.ofSeconds(600))
        );
        withExposedService(
            "kafka_1", 9092,
            Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(300))
        );
        withExposedService(
            "connect_1", 8083,
            Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(300))
        );
        withTailChildContainers(true);
    }


    public void start() {
        if (enabled) {
            super.start();
        }
    }


    public boolean isEnabled() {
        return enabled;
    }


}
