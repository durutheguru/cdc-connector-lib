## CDC Connector Lib ReadME.md

<br />

#### Introduction

Change Data Capture (CDC) is a technique used in the field of data management and data integration to capture and track changes made to a database in real-time. It enables the identification and capture of data modifications including: inserts, updates, and deletes, as they occur, and transformation of these changes into a format that can be consumed by other systems or processes.

The primary purpose of CDC is to capture and propagate data changes across multiple systems, databases, or applications, ensuring that data remains consistent and up to date across the entire ecosystem. It provides a way to synchronize data between heterogeneous systems without relying on periodic batch updates or manual data entry.

There are several strategies for CDC:
- Timestamp based: pull based approach where systems query the source based on the last updated timestamp. Source items with last updated timestamp above the threshold can be read for change updates.
- Trigger based: push based approach that can rely on using database events and triggers to propagate change events from a datasource.
- Log based: pull based approach which involves tailing the change log file for a datasource and looking for specific entries in the logs.

The CDC connector library is based on debezium connector. Which is a log based CDC system. Debezium tails the change log of a database and streams change events to a queue like Kafka.

![CDC](https://drive.google.com/u/0/uc?id=1Y70bHvQ2K477xpEayU0Dl9bcpJamLGKY&export=download)

src: https://www.confluent.io/learn/change-data-capture/


Let's imagine an architecture that looks like this.

- Employee Service: Manages Employee User information, like user details and address.
- Analytics  Service: Maintains live analytics on user information to extract insights and data.
- Relationship Manager Service: Maintains Relationship Managers and users they are assigned to.

![Sample Architecture](https://drive.google.com/u/0/uc?id=1m90DstqvAdlfte3SipdQaLFlAmSjaHTu&export=download)


In this setup, connector tails the change logs on each of the Service DBs, and streams change events to Kafka. Interested consumers can listen for those events and respond accordingly. In other words, one can receive notification of change events without explicitly coding a publisher on the source side. This helps decouple the change event from potential consumers of the event..

<br />


### Use cases for CDC

Example:
Consider a scenario where the Analytics service and RM service need to be notified soon as an Employee User’s address changes. I’ll present 3 solutions to the problem.

<br />

- **Solution 1 (Worst Solution)**: Within the API controller on the Employee Service that updates the user details, write code to explicitly notify each of the interested parties about the address change.

      Pros
      . might be the safest solution since we have some level of guarantee that Analytics and RM service endpoints are invoked to notify the change update.
    
      Cons
      . The least favorite solution, because the Employee Service will have to be explicitly aware of each of the other services: Analytics Service and RM Service. 
      . The Employee Service will have to maintain code that will call endpoints on Analytics Service and Relationship Manager Service which increases coupling.
      . In a case where we need to add another service later on that is interested in notifications of employee address change, then we have to modify Employee Service code.
  <br />

- **Solution 2 (Okay Solution)**: Within the API controller on the Employee Service that updates the employee user's details, write code to publish a message to a topic or channel that will target services interested in the change update.

  ```
  Pros
  . This solution works pretty well. After we update the details of the user, we can implement a publisher to write the update to a channel, which will be picked up by interested consumers. 
  . Employee service doesn't have to maintain code to explicitly call endpoints on Analytics Service and RM Service. 
  . If another service is added later that is interested in notifications of Employee address change, then we can simply add the service to the Consumers interested in change updates. No need to modify Employee service. 
      
  Cons
  . This solution relies on the API invocation of User Service. Because the publisher is coded within the controller flow. Hence if someone goes to the database to manually modify the Employee's address, the change update is not published to the consumers. 
  ```
  <br />


- **Solution 3 (Best Solution)**: Employ Change data capture. Stream change log updates to a message broker and have consumers read off topics on the broker. This will mean that when an employee's address is updated, the update log will be picked up and streamed to the message broker. Any interested consumers can subscribe to the topic and handle the update in their own way.

  ```
  Pros
  . This solution does not need the Employee Service to explicitly publish any events within its controller flow. Simply committing a transaction involving an address update will trigger CDC to pick up the event and push to our message broker. 
  . Employee service doesn't have to maintain code to explicitly call endpoints on Analytics Service and RM Service. 
  . If another service is added later that is interested in notifications of Employee address change, then we can simply add the service to the Consumers interested in change updates. No need to modify Employee service.
  . Even if the address is updated via command line, or workbench or any SQL tool, the change update is still picked up and streamed to the broker. 

  Cons
  . Slighly increased complexity, because now you need to add a Connector service within your infrastructure that is able to connect to your datasource and stream change updates.
  ```
  <br /> 



### CDC-Connector Lib (High level benefits)
- Easy configurations to start streaming change events from a datasource.
  Here's a Sample config to synchronize change updates from one datasource to another:

```
code.config.connector:
  url: http://localhost:8083
  source-connectors:
    - name: cdc_connector
      connectorType: MYSQL_SOURCE
      databaseHost: mysqldb
      databasePort: 3306
      databaseUsername: root
      databasePassword: 1234567890
      databaseIncludeList:
        - employee
      tableIncludeList:
        - employee.user
      kafkaBootstrapServers:
        - kafka:9092
      includeSchemas: true
      disableDefaultConsumer: true
  sink-connectors:
    - name: sink_connector
      connectorType: JDBC_SINK
      topics: cdc_connector.employee.user
      url: "jdbc:mysql://mysqldb:3306/employee_database_sink?createDatabaseIfNotExist=true&serverTimezone=UTC"
      username: root
      password: 1234567890

```

The CDC-Connector-Lib picks up your configurations and posts them to the connector url. The above config will sync all changes (insert, update, delete) from user table in employee database to employee_database_sink db. Explanation on how this works will come later.

- Durable consumer handlers that ensures retrial until you successfully process an event. Failed events are cycled in a DLQ.

- Ease of use. Declaring a change consumer is as simple as this:

```
@ChangeConsumer(sourceId = "employee.user", changeType = ChangeType.CREATE)
public class CreateUserChangeProcessor {


    public OperationStatus query(String reference, Payload payload) {
        // handle query logic
        return OperationStatus.pending();
    }


    public OperationStatus process(String reference, Payload payload) {
        // handle processing logic
        return OperationStatus.success();
    }


}

```

The above consumer will listen for new users CREATED in employee database. The cdc-connector-lib will first query the consumer with the reference and payload before calling process method. The consumer is retried until an OperationStatus.success() is returned.


- Sample Configurations:
  Now let's look at a few sample configurations to address different use cases.

For the sake of our further understanding, let's define 3 types of entities. Source, Processor and Sink.

**Source**: Like the name implies, a source is an origin of change events.

**Processor**: A processor subscribes to change events for the purpose of carrying out some action based on the event that has occurred.

**Sink**: A sink is a destination for change events. A Sink subscribes to one or more cdc topics and replicates change events on its own side.


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
      connectorType: MYSQL_SOURCE
      databaseHost: mysqldb
      databasePort: 3306
      databaseUsername: root
      databasePassword: 1234567890
      databaseIncludeList:
        - employee
      tableIncludeList:
        - employee.user
      kafkaBootstrapServers:
        - kafka:9092
      includeSchemas: true
      disableDefaultConsumer: true
```

For every table in the tableIncludeList, the connector will create topic of the format:

			{connector_name}.{database_name}.{table_name}

Setting up this configuration will cause the connector to create topic `cdc_connector.employee.user` on kafka which will house the change updates from the user table.
The `includeSchemas` config is important so schema data is included in the messages streamed to kakfa, this will allow the sink handler to properly replicate the events at the destination.
The `disableDefaultConsumer` config is important so connector lib will not bother creating a consumer for the change events, since we plan to replicate the events to a sink without consuming ourselves.


	Here's what the Sink configuration looks like: 
```

code.config.connector:
  url: http://localhost:8083
  sink-connectors:
    - name: sink_connector
      connectorType: JDBC_SINK
      topics: cdc_connector.employee.user
      url: "jdbc:mysql://mysqldb:3306/employee_database_sink?createDatabaseIfNotExist=true&serverTimezone=UTC"
      username: root
      password: 1234567890
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
      connectorType: MYSQL_SOURCE
      databaseHost: mysqldb
      databasePort: 3306
      databaseUsername: root
      databasePassword: 1234567890
      databaseIncludeList:
        - employee
      tableIncludeList:
        - employee.user
      kafkaBootstrapServers:
        - kafka:9092
```

This will read change events from the database into the kafka topic: `cdc_connector.employee.user`.

Run the test `CdcConnectorLibApplicationTests` and confirm it passes. The test loads a sql script and executes it. Ensuring that the change is captured in the processor. If you want the test to pause so you can inspect what's going on, simple add environment variable: `INSPECTION_ENABLED=true`

Open Akhq http://localhost:8181, And navigate to the topics menu, you should see the topic `cdc_connector.employee.user` has been created.
We also have the dlq topic: `dlq.group-id.cdc_connector.employee.user` where we'll cycle failed messages. The dlq topic will usually be of the format:

			dlq.{group_id}.{connector_name}.{database_name}.{table_name}

The group_id of the consumer that failed to consume the message is included in the topic name, so consumers with different group_ids don't reference the same dlq topic. Group Id is configured using the property: `queue.config.consumers.default-group-id`.

There are 2 processors defined in the test package `com.moniepoint.cdc.changeconsumer`: `CreateUserChangeProcessor`, `UpdateUserChangeProcessor`.
Inspect their code to gain an understanding of what they do.

```
@Slf4j
@RequiredArgsConstructor
@ChangeConsumer(sourceId = "employee.user", changeType = ChangeType.CREATE)
public class CreateUserChangeProcessor {


    private final DataCaptureMap dataCaptureMap;


    public OperationStatus query(String reference, Payload payload) {
        return OperationStatus.pending();
    }


    public OperationStatus process(String reference, Payload payload) {
        dataCaptureMap.put(reference, payload);
        return OperationStatus.success();
    }


}
```


```
@Slf4j
@RequiredArgsConstructor
@ChangeConsumer(sourceId = "employee.user", changeType = ChangeType.UPDATE)
public class UpdateUserChangeProcessor {


    private final DataCaptureMap dataCaptureMap;


    public OperationStatus query(String reference, Payload payload) {
        return OperationStatus.pending();
    }


    public OperationStatus process(String reference, Payload payload) {
        dataCaptureMap.put(reference, payload);
        return OperationStatus.success();
    }


}
```

Notice how both processors declare sourceId and changeType. The sourceId must be of the form `{database_name}.{table_name}`, matching an item included in the tableIncludeList of the source config.
The changeType can be either `CREATE`, or `UPDATE` or `DELETE`. Depending on the type of change event from the sourceId which the consumer wishes to consume.

ChangeConsumers must also declare query and process methods. With the signatures above. The reference can always be re-computed by calling `payload.hash()` and is guaranteed to be unique per payload.


<br />


### Understanding the code. CDC Lib Process Flow
- `ConnectorsBootstrapper`:
  <br /><br />
  The first step is registration of the connector configurations on a connector instance. This is to inform connector of what databases and tables we intend to stream change updates. If registration fails (for any reason except already existing connector config), the system throws an exception and the application is prevented from starting up.
  The library composes an API call to the connector instance: Check the `installConnector` method in ConnectorsBootstrapper class.

  The `ConnectorsBootstrapper` class is responsible for POSTing all the connectors (source and sink) on the connector instance. If any POST request fails, the application startup is halted with the exception clearly displayed.
  By default, the `ConnectorsBootstrapper` also sets up the consumers for the CDC change event topics and the DLQ topics for handling failures..


```
private void createConsumer(Consumer consumer, String... topics) throws Exception {
    log.debug("Creating consumer for topic: {}", String.join(", ", topics));

    MethodKafkaListenerEndpoint<String, String> endpoint = new MethodKafkaListenerEndpoint<>();

    endpoint.setId(UUID.randomUUID().toString());
    endpoint.setGroupId(groupId);
    endpoint.setBean(consumer);
    endpoint.setTopics(topics);
    endpoint.setMessageHandlerMethodFactory(new DefaultMessageHandlerMethodFactory());
    endpoint.setMethod(consumer.getClass().getMethod("consume", ConsumerRecord.class));

    Properties consumerProperties = new Properties();
    consumerProperties.putAll(kafkaProperties.buildConsumerProperties());
    endpoint.setConsumerProperties(consumerProperties);

    registry.registerListenerContainer(endpoint, cdcKafkaListenerContainerFactory, true);
}

```

This step is only necessary if you intend to consume these change events yourself in your application. If you don't intend to consume change events, for example if you're syncing from a data source to a sink, then you can disable consumer creation by setting property: `disableDefaultConsumer: true` on the connector config.

After successful registration, by default, the Bootstrapper creates Kafka Consumer for the CDC events and DLQ messages.
1. create consumer will be called with an instance of `CdcConsumer` and the topic to listen to for change events. This value will correspond to the topic we're expecting connector to stream those changes.
2. create consumer is also called with an instance of `CdcDlqConsumer`, passing in the dlq topic for the CDC events.

<br /><br />

- `CdcProcessorDelegate`:
  <br /><br />
  A CdcProcessorDelegate does the work of actually processing the change event payload. Annotated ChangeConsumers are eventually created as CdcProcessorDelegates (more on this later). CdcProcessorDelegate has the following interface.

```
public interface CdcProcessorDelegate {

    ....

    String sourceId();

    ChangeType type();

    boolean supports(Payload payload);

    OperationStatus query(String reference, Payload payload);

    OperationStatus process(String reference, Payload payload);

}

```

Let's run through each of them:
1. `sourceId`: represents a distinct source for change events. Typically, this will be a string of the format "{database_name}.{table_name}".
2. `type`: an enum value which represents the type of change event. There are 3 change types: CREATE, UPDATE, DELETE.
3. `supports`: the supports method accepts a payload and returns a boolean indicating whether the delegate can process the payload. Within the supports method, the delegate can implement logic referencing the payload argument to return true or false if it can process it.
4. `query`: query is a method that will be called to query the delegate with a change event payload and reference. The reference is usually a hash of the payload contents and is guaranteed to be unique for different payloads. The delegate must respond with an OperationStatus signifying if it has handled the payload. The OperationStatus has 5 possible values:

<br /><br />

```
PENDING("Pending"), // message processing has not been attempted

IN_PROGRESS("In Progress"), // message processing is currently ongoing

SUCCESSFUL("Successful"), // message processing was successful

FAILED("Failed"), // message processing failed

INVALID("Invalid"); // message is invalid and cannot be processed

```

If query method returns `PENDING` or `FAILED`, then the process method of the delegate will be called next..

5. `process`: this is the method that is called to process the change event payload. The delegate must respond with an `OperationStatus` signifying if it has handled the payload. If the process method returns anything asides a final status (`SUCCESSFUL` | `INVALID`), the payload will be written to DLQ.

<br /><br />

- `CdcProcessorDelegateContainer`:
  <br /><br />
  The `CdcProcessorDelegateContainer` contains all the registered delegates. The container also serves as an aggregator, exposing 'supports', 'query' and 'process' methods that cycle through the registered delegates and target the delegate which supports the payload. While querying and processing, exactly one delegate must support the payload. If no delegate or multiple supporting delegates are found, it results in an error.
  <br /><br />
- `CdcBeanProcessor`:
  <br /><br />
  This component is a Spring BeanPostProcessor that handles registration of all annotated Change Consumers on the `CdcProcessorDelegateContainer`. Hence is only interested in classes annotated with `@ChangeConsumer`.


```
@Override
public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    try {
        if (hasChangeConsumer(bean)) {
            log.debug("Encountered Bean with actions: {}", beanName);
            registerConsumer(bean);
        }

        return bean;
    }
    catch (Exception e) {
        throw new RuntimeException(e);
    }
}


private boolean hasChangeConsumer(Object bean) {
    ChangeConsumer consumer = bean.getClass().getAnnotation(ChangeConsumer.class);
    return consumer != null;
}
```

This class handles validation of the ChangeConsumers, ensuring that the query and process methods are present and of the right signature.

```
private void registerConsumer(Object bean) throws Exception {
    ChangeConsumer consumer = bean.getClass().getAnnotation(ChangeConsumer.class);
    Class<?> beanClass = bean.getClass();

    try {
        Method queryMethod = beanClass.getMethod(ChangeConsumer.QUERY_METHOD_NAME, String.class, Payload.class);
        Method processMethod = beanClass.getMethod(ChangeConsumer.PROCESS_METHOD_NAME, String.class, Payload.class);

        validateMethodReturnType(queryMethod, processMethod);
        doRegistration(consumer, bean, queryMethod, processMethod);
    }
    catch (NoSuchMethodException ex) {
        throw new IllegalStateException(
            String.format(
                "Consumer {%s} must have a query and process method. Supported signatures: %n" +
                "- OperationStatus query(String reference, Payload payload) %n" +
                "- OperationStatus process(String reference, Payload payload)%n%n",
                beanClass.getName()
            )
        );
    }
}

```

The doRegistration method creates an anonymous implementation of the CdcProcessorDelegate and registers it on the CdcProcessorDelegateContainer.

- `CdcKafkaProducerConfig` and `CdcKafkaConsumerConfig`:
  <br /><br />
  both configs handle the CDC producer and consumer configurations for Kafka. These configurations use the `spring.kafka` prefixed configurations declared in application properties. explicitly setting the key and value serializers to StringSerializer. Also optional SASL configurations are included for SSL support.


```
Map<String, Object> props = kafkaProperties.buildConsumerProperties();
props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

cdcSaslConfiguration.configureSasl(props);

return props;
```

In the `KafkaListenerContainerFactory` bean, the  `CdcKafkaConsumerConfig` sets the error handler to a `DefaultErrorHandler` type, with back-off currently hardcoded to `FixedBackOff` (..to be made configurable soon).

```
public DefaultErrorHandler errorHandler() {
    DLQPublisher[] recoverer = new DLQPublisher[1];

    DefaultErrorHandler errorHandler = new DefaultErrorHandler(
        recoverer[0] = publishingRecoverer(),
        new FixedBackOff(0L, 0L)
    ); // TODO: Back-Off should be configurable

    errorHandler.setRetryListeners(
        (record, ex, deliveryAttempt) -> {
            log.error(
                String.format(
                    "Failed Record in Retry Listener. Exception : %s, deliveryAttempt: %s",
                    ex.getMessage(), deliveryAttempt
                ), ex
            );
            recoverer[0].accept(record, ex);
        }
    );

    return errorHandler;
}

```

This means as soon as errors are encountered, it's dropped back in the DLQ using the recoverer's accept method. the recoverer is initialized with a DLQPublisher. Check the publishingRecoverer method call. The recoverer is built with a dlqProducerRecordFactory which does the actual work of creating the producer record to be dropped back in the DL Queue.

- DLQ : By default, the error handler will write failed messages back to the DLQ topic. The dlq topic name is decided by a CdcDlqPrefixHandler. This prefix handler, appends a prefix to the original CDC topic name. The method called is  addDLQPrefix . Current implementation returns a string of the format dlq.{group-id}.{topic_name}.


- Change Consumer Samples:
  The Change Consumers are processors that are targeted after a message is successfully read from Kafka. Change Consumers are created using the `@ChangeConsumer` annotation. Here's an example usage:

```
@ChangeConsumer(sourceId = "database.user", changeType = ChangeType.CREATE)
```

The `ChangeConsumer` annotation is annotated with `@Component`. So change consumers are legit spring components that will be class-path scanned.
```
@Component
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ChangeConsumer {

    ...

    String sourceId();

    ChangeType changeType();

    ...

}

```


ChangeConsumer annotation declares sourceId and changeType parameters. The sourceId should be of the format `{database_name}.{table_name}`; changeType can be either `CREATE`, or `UPDATE` or `DELETE`. Depending on the type of change event from the sourceId which the consumer wishes to consume.

ChangeConsumers must also declare query and process methods. Here's a sample consumer:

```
@Slf4j
@RequiredArgsConstructor
@ChangeConsumer(sourceId = "database.user", changeType = ChangeType.CREATE)
public class CreateUserChangeProcessor {


    private final DataCaptureMap dataCaptureMap;


    public OperationStatus query(String reference, Payload payload) {
        ... // implement query logic
        return OperationStatus.pending();
    }


    public OperationStatus process(String reference, Payload payload) {
        ... // do processing
        return OperationStatus.success();
    }


}

```


<br />

### Scalability
Scalability is a very important concern given the amount of processing our systems must support regularly. Kafka achieves scalability with concepts like partitions, batching polling, consumer groups, parallel processing etc. CDC-lib currently has leverages for partitioning and consumer groups. You can configure the number of partitions that will be used to create the topics for CDC events. Currently, this value is not configurable per topic. Use the config: `queue.config.topics.partitions` to set the number of partitions for CDC topics. Use the config: `queue.config.topics.replication` to set the replication factor for the CDC topic.

Bear in mind, to reap the full benefits of partitioning, you'll need to have at least n consumer instances for the topic in the consumer group. Where n is the number of partitions. If n is greater than one, then you loose ordering of database events, since data will arrive at different partitions which will be handled in different consumers.

Be sure to configure the default consumer group id using property: `queue.config.consumers.default-group-id`. This value should be unique per service. So multiple instances of the same service will be in the same consumer group. You can scale your consumers to read off the CDC topics.


