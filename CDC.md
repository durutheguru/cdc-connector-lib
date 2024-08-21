## Change Data Capture

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

