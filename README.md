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