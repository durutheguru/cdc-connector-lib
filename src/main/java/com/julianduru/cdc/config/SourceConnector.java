package com.julianduru.cdc.config;

import lombok.Data;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * created by Julian Duru on 28/04/2023
 */
@Data
public class SourceConnector {

    public static String KAFKA_BOOTSTRAP_SERVERS_OVERRIDE;


    static final Random random = new Random(System.currentTimeMillis());


    @NotEmpty(message = "Connector Config name should not be empty")
    private String name;


    @NotNull(message = "Connector Config connector type should not be empty")
    private ConnectorType connectorType;


    @NotEmpty(message = "Connector Config database host should not be empty")
    private String databaseHost;


    @NotNull(message = "Connector Config database port should not be empty")
    private Integer databasePort;


    @NotEmpty(message = "Connector Config database username should not be empty")
    private String databaseUsername;


    private String databasePassword;


    @NotEmpty(message = "Connector Config database include list should not be empty")
    private List<String> databaseIncludeList;


    @NotEmpty(message = "Connector Config table include list should not be empty")
    private List<String> tableIncludeList;


    @NotEmpty(message = "Connector Config kafka bootstrap servers should not be empty")
    private List<String> kafkaBootstrapServers;


    private boolean includeSchemas;


    private boolean disableDefaultConsumer;


    public Map<String, String> generateConfigMap() {
        Map<String, String> map = new HashMap<>();

        map.put("connector.class", connectorType.getConnectorClass());
        map.put("tasks.max", "1");
        map.put("database.hostname", databaseHost);
        map.put("database.port", databasePort.toString());
        map.put("database.user", databaseUsername);
        map.put("database.password", databasePassword);
        map.put("database.allowPublicKeyRetrieval", "true");
        map.put("database.server.id", String.valueOf(Math.abs(random.nextLong()/1000)));
        map.put("database.server.name", getName());
        map.put("database.connectionTimeZone", "UTC");
        map.put("topic.prefix", getName());
        map.put("database.include.list", String.join(",", databaseIncludeList));
        map.put("table.include.list", String.join(",", tableIncludeList));
        map.put("database.history.kafka.bootstrap.servers", getKafkaBootStrapServers());
        map.put("database.history.kafka.topic", "schema-changes." + getName());
        map.put("numeric.mapping", "best_fit");
        map.put("value.converter", "org.apache.kafka.connect.json.JsonConverter");
        map.put("value.converter.schemas.enable", String.valueOf(includeSchemas));
        map.put("include.schema.changes", "true");
        map.put("schema.history.internal.kafka.bootstrap.servers", getKafkaBootStrapServers());
        map.put("schema.history.internal.kafka.topic", "schema-changes." + getName());
//        map.put("num.partitions", "6"); //TODO: hard-coded, should be configurable


        return map;
    }


    private String getKafkaBootStrapServers() {
        return StringUtils.hasText(KAFKA_BOOTSTRAP_SERVERS_OVERRIDE) ? KAFKA_BOOTSTRAP_SERVERS_OVERRIDE :
            String.join(",", kafkaBootstrapServers);
    }


}


