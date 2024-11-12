
### CDC-Connector Lib - README.md

##### Setup 
The Connector Lib is a library and hence is meant to be included as part of an application program or service. To get a 
feel of how it works. You can run the tests while enabling peek_mode. 

To run the tests in peek mode, simply set environment variable `PEEK_MODE=enable`. 


##### Overview

- Easy configurations to start streaming change events from a datasource.
  Here's a Sample config to synchronize change updates from one datasource to another:

```
code.config.connector:
  url: http://localhost:8083
  
  source-connectors:
     - name: cdc_connector
        config:
          connector.class: io.debezium.connector.mysql.MySqlConnector
          offset.storage: org.apache.kafka.connect.storage.FileOffsetBackingStore
          offset.storage.file.filename: /tmp/offsets1.dat
          tasks.max: 1
          database.hostname: mysqldb
          database.port: 3306
          database.user: root
          database.password: 1234567890
          database.allowPublicKeyRetrieval: true
          database.server.id: 5
          database.server.name: ${code.config.connector.source-connectors[0].name}
          database.connectionTimeZone: 'UTC'
          topic.prefix: ${code.config.connector.source-connectors[0].name}
          database.include.list: employee
          table.include.list: employee.user
          numeric.mapping: best_fit
          value.converter: org.apache.kafka.connect.json.JsonConverter
          value.converter.schemas.enable: true
          include.schema.changes: true
          schema.history.internal: io.debezium.storage.file.history.FileSchemaHistory
          schema.history.internal.file.filename: /tmp/schema_history.dat
        disable-default-consumer: true


  sink-connectors:
    - name: sink_connector
      config:
        connector.class: "io.debezium.connector.jdbc.JdbcSinkConnector"
        topics: cdc_connector.employee.user
        connection.url: "jdbc:postgresql://postgres:5432/employee_database_sink"
        connection.username: postgres
        connection.password: password
        tasks.max: 1
        insert.mode: "upsert"
        delete.enabled: true
        primary.key.mode: "record_key"
        schema.evolution: "basic"
        database.time_zone: "UTC"


```

The CDC-Connector-Lib picks up your configurations and posts them to the connector url. The above config will sync all changes (insert, update, delete) from user table in employee database to employee_database_sink db. Explanation on how this works will come later.

- Durable consumer handlers that ensures retrial until you successfully process an event. Failed events are cycled in a DLQ.

- Ease of use. Declaring a change consumer is as simple as this:

```
@ChangeConsumer(sourceId = "employee.user", changeType = ChangeType.CREATE)
public class CreateUserChangeProcessor {


    public void process(Payload payload) {
        log.debug("New User inserted: {}", JSON.stringify(payload));
        // handle logic for inserted user
    }


}
```

The above consumer will listen for new users CREATED in employee database. The cdc-connector-lib will first query the consumer with the reference and payload before calling process method. The consumer is retried until an OperationStatus.success() is returned.


- Sample Configurations:
  Now let's look at a few sample configurations to address different use cases.

For the sake of our further understanding, let's define 3 types of entities. Source, Processor and Sink.

**Source**: Like the name implies, a source is an origin of change events.

**Processor**: A processor subscribes to change events for the purpose of carrying out some action based on the event that has occurred.

**Sink**: A sink is a destination for change events.


<br />


### Example Patterns and Configurations
- Sync Database events between 2 databases: In this setup, we have two separate services with different databases. We want to sync database events from one db to the other.
  Look at test `DatabaseSyncTest`. We want to run the test, and have it pause with the join statement, so we can look at what's going on in slow motion. in your test config, set `INSPECTION_ENABLED=true`. And run the test.
  Every time a change happens on the 'user' table in employee database, we wish to sync the update to a second database.

  Look at the properties file: `application-db-sync.yml` to have a picture of what the configuration looks like.

  On the source side, we have a configuration like this:
```
code.config.connector:
  url: http://localhost:8083
  source-connectors:
    - name: cdc_connector
      config:
        connector.class: io.debezium.connector.mysql.MySqlConnector
        offset.storage: org.apache.kafka.connect.storage.FileOffsetBackingStore
        offset.storage.file.filename: /tmp/offsets1.dat
        tasks.max: 1
        database.hostname: mysqldb
        database.port: 3306
        database.user: root
        database.password: 1234567890
        database.allowPublicKeyRetrieval: true
        database.server.id: 5
        database.server.name: ${code.config.connector.source-connectors[0].name}
        database.connectionTimeZone: 'UTC'
        topic.prefix: ${code.config.connector.source-connectors[0].name}
        database.include.list: employee
        table.include.list: employee.user
        numeric.mapping: best_fit
        value.converter: org.apache.kafka.connect.json.JsonConverter
        value.converter.schemas.enable: true
        include.schema.changes: true
        schema.history.internal: io.debezium.storage.file.history.FileSchemaHistory
        schema.history.internal.file.filename: /tmp/schema_history.dat
      disable-default-consumer: true
```

For every table in the tableIncludeList, the connector will create topic of the format:

			{connector_name}.{database_name}.{table_name}

Setting up this configuration will cause the connector to create topic `cdc_connector.employee.user` on kafka which will house the change updates from the user table.
The `include.schemas.changes` config is important so schema data is included in the messages streamed to kakfa, this will allow the sink handler to properly replicate the events at the destination.
The `disableDefaultConsumer` config is important so connector lib will not bother creating a consumer for the change events, since we plan to replicate the events to a sink without consuming ourselves.


	Here's what the Sink configuration looks like: 
```
code.config.connector:
  url: http://localhost:8083
  sink-connectors:
  - name: sink_connector
    config:
      connector.class: "io.debezium.connector.jdbc.JdbcSinkConnector"
      topics: cdc_connector.employee.user
      connection.url: "jdbc:postgresql://postgres:5432/employee_database_sink"
      connection.username: postgres
      connection.password: password
      tasks.max: 1
      insert.mode: "upsert"
      delete.enabled: true
      primary.key.mode: "record_key"
      schema.evolution: "basic"
      database.time_zone: "UTC"
```

The topic referenced must correspond to the topic name which will hold the events from the source config. Hence we have `cdc_connector.employee.user`.

If you run the test in class DatabaseSyncTest, once the test is running, it should hang at the join statement, then open akhq to view kafka. Open your browser address http://localhost:8181.

You can inspect the topics on akhq, to get a feel of the kind of data they contain. Also, connect to the mysql instance running in the integration test (check the `docker-compose.yml` file, you will find the mysql credentials). In the mysql instance, there are 2 databases employee and employee_database_sink.

Updates to employee database are automatically replicated to employee_database_sink. Play with it for a bit and see the results. Insert, Update, Delete..



- Source and Processor
  In some cases, you want to capture database change events at a source and process them in some way without necessarily syncing them to a destination database. In which case you will configure only the source side like this:

```
code.config.connector:
  url: http://localhost:8083
  source-connectors:
  - name: cdc_connector
    config:
      connector.class: io.debezium.connector.mysql.MySqlConnector
      offset.storage: org.apache.kafka.connect.storage.FileOffsetBackingStore
      offset.storage.file.filename: /tmp/offsets0.dat
      tasks.max: 1
      database.hostname: localhost
      database.port: 33080
      database.user: root
      database.password: 1234567890
      database.allowPublicKeyRetrieval: true
      database.server.id: 5
      database.server.name: ${code.config.connector.source-connectors[0].name}
      database.connectionTimeZone: 'UTC'
      topic.prefix: ${code.config.connector.source-connectors[0].name}
      database.include.list: employee
      table.include.list: employee.user
      numeric.mapping: best_fit
      value.converter: org.apache.kafka.connect.json.JsonConverter
      value.converter.schemas.enable: false
      include.schema.changes: false
      schema.history.internal: io.debezium.storage.file.history.FileSchemaHistory
      schema.history.internal.file.filename: /tmp/schema_history.dat
    disable-default-consumer: false
```

This will read change events from the database into the kafka topic: `cdc_connector.employee.user`.

Run the test `CdcConnectorLibApplicationTests` and confirm it passes. The test loads a sql script and executes it. Ensuring that the change is captured in the processor. If you want the test to pause so you can inspect what's going on, simple add environment variable: `INSPECTION_ENABLED=true`

Open Akhq http://localhost:8181, And navigate to the topics menu, you should see the topic `cdc_connector.employee.user` has been created.
We also have the dlq topic: `dlq.group-id.cdc_connector.employee.user` where we'll cycle failed messages. The dlq topic will usually be of the format:

			dlq.{group_id}.{connector_name}.{database_name}.{table_name}

The group_id of the consumer that failed to consume the message is included in the topic name, so consumers with different group_ids don't reference the same dlq topic. Group Id is configured using the property: `queue.config.consumers.default-group-id`.

There are 2 processors defined in the test package `com.julianduru.cdc.changeconsumer`: `CreateUserChangeProcessor`, `UpdateUserChangeProcessor`.
Inspect their code to gain an understanding of what they do.

```
@Slf4j
@RequiredArgsConstructor
@ChangeConsumer(sourceId = "employee.user", changeType = ChangeType.CREATE)
public class CreateUserChangeProcessor {


    public void process(Payload payload) {
        log.debug("New User inserted: {}", JSON.stringify(payload));
        // handle logic for inserted user
    }


}

```


```
@Slf4j
@RequiredArgsConstructor
@ChangeConsumer(sourceId = "employee.user", changeType = ChangeType.UPDATE)
public class UpdateUserChangeProcessor {


    private final DataCaptureMap dataCaptureMap;


    public void process(String reference, Payload payload) {
        dataCaptureMap.put(reference, payload);
    }


}
```

Notice how both processors declare sourceId and changeType. The sourceId must be of the form `{database_name}.{table_name}`, matching an item included in the tableIncludeList of the source config.
The changeType can be either `CREATE`, or `UPDATE` or `DELETE`. Depending on the type of change event from the sourceId which the consumer wishes to consume.

ChangeConsumers must also declare query and process methods. With the signatures above. The reference can always be re-computed by calling `payload.hash()` and is guaranteed to be unique per payload.


<br />

