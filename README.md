[![Build Status](https://travis-ci.org/gregbugaj/hoplin.io.svg?branch=master)](https://travis-ci.org/gregbugaj/hoplin.io)

# hoplin.io
A lightweight RabbitMQ client for Java (built on top of rabittmq java client)

To make working with RabbitMQ as simple as possible with minimum dependencies.

# Quick start

## Connecting to a broker

## Publishing 

# Exchange Clients

## Fanout Exchange Client

## Direct Exchange Client

# RPC Client / Server

## RPC client

## RPC server

## Exchanges and Queues

## Error handling

Sample `MessageError` with original payload.

Messages are routed to efault error exchange : `hoplin.error.exchange`

```json
{
  "exchange": "exchange.batch",
  "queue": "test:exchange.batch:examples.LogDetail",
  "routingKey": "",
  "creationTime": 1582627056671,
  "exception": "examples.batch.ReceiveBatchJob.handleWithReturn(ReceiveBatchJob.java:34)\n",
  "body": "{\n  \"status\": 0,\n  \"payload\": {\n    \"msg\": \"Msg \\u003e\\u003e 738745336710241\",\n    \"level\": \"info\"\n  },\n  \"type\": \"examples.LogDetail\",\n  \"ctime\": 1582627056560,\n  \"_payload_type_\": \"examples.LogDetail\"\n}",
  "properties": {
    "headers": {
      "x-batch-id": {
        "bytes": "ODljOGIyOGUtYWYyZS00NGM2LWIzNWQtNDU2NzA2YTI2YTVj"
      },
      "x-batch-correlationId": "77646972-e269-42f5-9b5b-388c27c13a86"
    },
    "correlationId": "77646972-e269-42f5-9b5b-388c27c13a86",
    "replyTo": "batch.documents.reply-to.dd846234-bcce-466a-a841-360e9f9aa356",
    "bodySize": 204
  }
}
```

## Metrics
Metrics can be added to the current client

```java
    FunctionMetricsPublisher
        .consumer(EmitLogTopic::metrics)
        .withInterval(1, TimeUnit.SECONDS)
        .withResetOnReporting(false)
        .build()
        .start();
```

Signature for the reporting method 
```java
  void metrics(final Map<String, Map<String, String>> o) {
    System.out.println(String.format("Metrics Info : %s", o));
  }
```

# Client Interoperability
The client is able to communicate between different RabbitMQ client (C#, Python, Ruby, etc...)
by using reflection based message parsing. This means that there is no need for `MessagePayload` envelope,
except when using RPC client. Trade of here is speed and lack of message polymorphism. Messages wrapped
in envelope do not need to perform `type` determination. 

### With envelope
```json5
{
  "status": 0,
  "payload": {
               "msg": "Msg : 27819763881153",
               "level": "info"
             },
  "type": "io.hoplin.model.LogDetail",
  "ctime": 0,
  "_payload_type_": "io.hoplin.model.LogDetail"
}
```

### Without envelope
```json
{
  "msg": "Msg : 27819763881153",
  "level": "info"
}
```

```java
public class LogDetail {
  private String msg;
  private String level;

  public LogDetail(final String msg, final String level) {
    this.msg = msg;
    this.level = level;
  }
}
```

`hoplin_default_error_queue`

# Code Style
[Google Style Guides](https://github.com/google/styleguide)

# Resources

[RabbitMQ](https://www.rabbitmq.com/)

[RawRabbit](https://github.com/pardahlman/RawRabbit)

[EasyNetQ](https://github.com/EasyNetQ/EasyNetQ)


OSSRH-43588

# Notes
https://stackoverflow.com/questions/1062113/fastest-way-to-write-huge-data-in-text-file-java
https://stackoverflow.com/questions/44647589/java-how-to-efficiently-write-a-sequential-file-with-occassional-holes-in-it


https://github.com/EasyNetQ/EasyNetQ/blob/2573e8a76a8ccf57f2477672782d8f8a5afafe76/Source/EasyNetQ/Consumer/DefaultConsumerErrorStrategy.cs
https://stackoverflow.com/questions/32189335/easynetq-custom-error-queue-name-based-on-the-original-queue