[![Build Status](https://travis-ci.org/gregbugaj/hoplin.io.svg?branch=master)](https://travis-ci.org/gregbugaj/hoplin.io)

# hoplin.io
A lightweight RabbitMQ client for Java (built on top of rabittmq java client)

To make working with RabbitMQ as simple as possible with minimum dependencies.

# Quick start


## Add dependency to your project 

Apache Maven
```java
	<dependency>
		<groupId>io.hoplin</groupId>
		<artifactId>hoplin-client</artifactId>
		<version>1.1.3</version>
	</dependency>
```

Gradle Groovy DSL
```java
implementation 'io.hoplin:hoplin-client:1.1.3'
```

Gradle Kotlin DSL
```java
implementation("io.hoplin:hoplin-client:1.1.3")
```



## Connecting to a broker

Minimal example that will bootstrap a `direct` exchange client and connect to local instance 
of RabbitMQ using sensible defaults.

```java
  ExchangeClient client = ExchangeClient.direct(RabbitMQOptions.defaults(), "awesome-exchange");
```

At this point your are able to publish and create subscriptions to the `awesome-exchange`

The `RabbitMQOptions` can be created in couple different ways.

**Default**

Using `defaults` will connect to localhost instance of RabbitMQ, great way to get up and running quickly 
during development not something we want to use for production.

```java
  RabbitMQOptions options = RabbitMQOptions.defaults();
```
**Connection String**

Parse the connection string in format `key1=value;key2=value`

```java
 RabbitMQOptions options = RabbitMQOptions.from("host=localhost;virtualHost=vhost1;username=user;password=secret");
```

Supported properties
```java
    host
    virtualhost
    username
    password
    requestedheartbeat
    timeout
    product
    platform
    connectionretries
    connectionretrydelay
```

**RabbitMQOptions Object**

Providing `options` directly gives the most flexibility.

```java

    /**
     * Create 'default' connection options
     * @return
     */
    protected static RabbitMQOptions options(final String host)
    {
        final RabbitMQOptions options =  new RabbitMQOptions();
        options.setConnectionRetries(3);
        options.setConnectionRetryDelay(250L);
        options.setHost(host);

        return options;
    }

``` 

# Exchange Clients
Support for common topologies have been abstracted into an `ExchangeClient`

**Topologies**

```java
    direct
    fanout
    header
    topic
    rpc   - custom
    batch - custom
```

Clients are crated providing a `Binding` or parameters directly.

`Binding` represent exchange/queue topology

Basic fanout exchange 
```java  
    final Binding binding = BindingBuilder
        .bind()
        .to(new FanoutExchange(EXCHANGE));
```

Topic binding with custom options
```java
    final Binding binding = BindingBuilder
        .bind()
        .to(new TopicExchange(EXCHANGE))
        .withAutoAck(true)
        .withPrefetchCount(1)
        .withPublisherConfirms(true)
        .build();
```

## Exchange Client (Direct / Fanout / Topic)
Creating Exchange Client is almost identical for most of the topologies, big different is the way 
the queue is bound to exchange. Check out the 'examples' for all examples and definitions.

**Publisher**

```java
   private static final String EXCHANGE = "direct_logs";
    public static void main(final String... args) throws InterruptedException {
      log.info("Starting producer on exchange : {}", EXCHANGE);
      final ExchangeClient client = ExchangeClient.direct(options(), EXCHANGE);
      client.publish(createMessage("info"), "info");

    }
  
    private static LogDetail createMessage(final String level) {
      return new LogDetail("Msg : " + System.nanoTime(), level);
    }
```    

**Subscriber**
```java
private static final String EXCHANGE = "direct_logs";

  public static void main(final String... args) throws InterruptedException {
    final ExchangeClient client = informative();
    final SubscriptionResult subscription = client
        .subscribe("test", LogDetail.class, msg -> {log.info("Message received [{}]", msg);});

    info(subscription);
    Thread.currentThread().join();
  }

  private static ExchangeClient critical() {
    return ExchangeClient
        .direct(options(), EXCHANGE, "log.critical", "error");
  }

  private static ExchangeClient informative() {
    return ExchangeClient
        .direct(options(), EXCHANGE, "log.informative", "info");
  }
```

## Exchange Client (Header)
Header exchange shows how messages can be customized via the `Configuration` argument.

**Publisher**

```java
  private static final String EXCHANGE = "header_logs";

  public static void main(final String... args) throws InterruptedException {
    log.info("Starting header producer for exchange : {}", EXCHANGE);
    final ExchangeClient client = ExchangeClient.header(options(), EXCHANGE);

    client.publish(createMessage("info"), cfg ->
    {
      cfg.addHeader("type", "info");
      cfg.addHeader("category", "service-xyz");
    });
  }

  private static LogDetail createMessage(final String level) {
    return new LogDetail("Msg : " + System.nanoTime(), level);
  }
```

**Subscriber**

```java
  private static final String EXCHANGE = "header_logs";

  public static void main(final String... args) throws InterruptedException {
    log.info("Starting header consumer for exchange : {}", EXCHANGE);
    final ExchangeClient client = clientFromBinding(EXCHANGE, "info", "service-xyz");
    client.subscribe("test", LogDetail.class, ReceiveLogHeader::handler);

    Thread.currentThread().join();
  }

  private static void handler(final LogDetail detail) {
    log.info("Message received :  {} ", detail);
  }

  private static ExchangeClient clientFromBinding(String exchange, String type, String category) {
    final Binding binding = BindingBuilder
        .bind("header_log_info_queue")
        .to(new HeaderExchange(exchange))
        .withArgument("x-match", "all")
        .withArgument("type", type)
        .withArgument("category", category)
        .build();

    return ExchangeClient.header(options(), binding);
  }
```

**Message customization**

Messages can be customized before they are published. 

```java
    client.publish(createMessage("info"), cfg ->
    {
      cfg.addHeader("type", "info");
      cfg.addHeader("category", "service-xyz");
    });
```

## Multiple message handlers / Polymorphic messages

Multiple message types can be published to the same exchange. 
 
**Publisher**

```java
  private static final String EXCHANGE = "mh_logs";

  public static void main(final String... args) throws InterruptedException {
    final Binding binding = bind();
    log.info("Binding : {}", binding);
    final ExchangeClient client = ExchangeClient.fanout(options(), binding);

    client.publish(new LogDetail("DetailType A", "info"));
    client.publish(new LogDetailType2("DetailType B", "info"));
  }

  private static Binding bind() {
    return BindingBuilder
        .bind()
        .to(new FanoutExchange(EXCHANGE));
  }
```

**Subscriber**

```java

  private static final String EXCHANGE = "mh_logs";

  public static void main(final String... args) throws InterruptedException {
    final ExchangeClient client = FanoutExchangeClient.create(options(), EXCHANGE);

    client.subscribe("test", LogDetail.class, MultipleTypesReceiver::handle1);
    client.subscribe("test", LogDetailType2.class, MultipleTypesReceiver::handle2);

    Thread.currentThread().join();
  }

  private static void handle1(final LogDetail msg, final MessageContext context) {
    log.info("Handler-1  >  {}", msg);
  }

  private static void handle2(final LogDetailType2 msg, final MessageContext context) {
    log.info("Handler-2  >  {}", msg);
  }
}
```


## Same Producer/Consumer

Client can serve both as a Producer and Consumer

```java

  private static final String EXCHANGE = "topic_logs";

  public static void main(final String... args) throws InterruptedException {
    log.info("Starting producer/consumer for exchange : {}", EXCHANGE);
    final ExchangeClient client = ExchangeClient.topic(options(), EXCHANGE);
    client.subscribe("Test", LogDetail.class, SamePublisherConsumerExample::handle);

    for (int i = 0; i < 5; ++i) {
      client.publish(createMessage("info"), "log.info.info");
      client.publish(createMessage("debug"), "log.info.debug");

      Thread.sleep(1000L);
    }
  }

  private static void handle(final LogDetail msg) {
    log.info("Incoming msg : {}", msg);
  }

  private static LogDetail createMessage(final String level) {
    return new LogDetail("Msg : " + System.nanoTime(), level);
  }
```

## Message Context


# RPC Client / Server
Client supports both Direct-Reply and Queue per Request/Response patterns. 

For `Direct-Reply` leave blank or use `amq.rabbitmq.reply-to`
If `direct-reply` is not used new queue will be create in format `{queumame}.response.{UUID}`
Queue = Name of the queue we will use for 'Reply-To' messages
Exchange = name of the exchange we want to bind our queue to
      
```java
  private static Binding bind() {    
    return BindingBuilder
        .bind("rpc.request.log")
        .to(new DirectExchange("exchange.rpc.logs"))
        .build()
        ;
  }
```

Typical structure of RPC client and server 

```java
    RpcClient<LogDetailRequest, LogDetailResponse> client = DefaultRpcClient.create(options(), bind());
    
    // rpc response
    client.respondAsync((request)->
    {   
        // do some heavy lifting
        return new LogDetailResponse("Response message", "info");
    });
    
    // rpc request
    final LogDetailResponse response = client.request(new LogDetailRequest("Request message", "info"));
    log.info("RPC response : {} ", response);
```

RPC client supports both synchronous and asynchronous processing, all methods have a corresponding `async`
method that will return a `CompletableFuture`

**Synchronous**

```java
    final LogDetailResponse response = client.request(new LogDetailRequest("Request message", "info"));
    log.info("RPC response : {} ", response);
```

**Asynchronous**

```java
  client
      .requestAsync(new LogDetail("Msg : " + System.nanoTime(), "info"))
      .whenComplete((reply, t) -> {        
        log.info("RPC response : {} ", reply);
        latch.countDown();
      });
```

## RPC client example

```java
 
  public static void main(final String... args) throws IOException {
    final Binding binding = bind();
    log.info("Binding : {}", binding);

    // Blocking
    final RpcClient<LogDetailRequest, LogDetailResponse> client = DefaultRpcClient
        .create(options(), binding);

    final LogDetailResponse response = client
        .request(new LogDetailRequest("Request message 1", "info"));
  }

  private static Binding bind() {
    return BindingBuilder
        .bind("rpc.request.log")
        .to(new DirectExchange("exchange.rpc.logs"))
        .build()
        ;
  }
```

## RPC server example

```java
  public static void main(final String... args) throws InterruptedException {
    final Binding binding = bind();
    log.info("Binding : {}", binding);

    final RpcServer<LogDetailRequest, LogDetailResponse> server = DefaultRpcServer
        .create(options(), binding);
    server.respondAsync(RpcServerExample::handler);
    Thread.currentThread().join();
  }

  private static LogDetailResponse handler(final LogDetailRequest log) {
    return new LogDetailResponse("response", "info");
  }

  private static Binding bind() {
    return BindingBuilder
        .bind("rpc.request.log")
        .to(new DirectExchange("exchange.rpc.logs"))
        .build()
        ;
  }
```

## RabittMQ batch message processing

There are times when we want to fire set of jobs and be notified when all of them complete. 
This can be easily accomplished with the latest version of hoplin.io RabbitMQ client.

Under the hood the batches are tracked via two custom properties `x-batch-id` and `x-batch-correlationId`,

**Batch job publisher**

We start by creating a new client and then enqueuing number of jobs to process, 
upon completion we display the time it took to complete all jobs. 
Client will attempt to use Direct-Reply queue if available.

```java
    public static void main(final String... args) throws InterruptedException {
      final CountDownLatch latch = new CountDownLatch(1);
      final BatchClient client = new DefaultBatchClient(options(), bind());
  
      client.startNew(context ->
      {
        for (int i = 0; i < 2; ++i) {
          context.enqueue(() -> new LogDetail("Msg >> " + System.nanoTime(), "info"));
          context.enqueue(() -> new LogDetail("Msg >> " + System.nanoTime(), "warn"));
        }
      })
      .whenComplete((context, throwable) ->
      {
        log.info("Batch completed in : {}", context.duration());
        latch.countDown();
      });
  
      latch.await();
    }
  
    private static Binding bind() {
      return BindingBuilder
          .bind("batch.documents")
          .to(new DirectExchange("exchange.batch"))
          .build()
          ;
    }
```

**Batch job receiver**

The subscriber is just a binding to a queue. The key is the return type of the handler for 
`Reply<LogDetail>`. Currently the value has to be wrapped with the `Reply` objects.


```java
public class ReceiveBatchJob extends BaseExample {

  private static final Logger log = LoggerFactory.getLogger(ReceiveBatchJob.class);

  private static final String EXCHANGE = "exchange.batch";

  public static void main(final String... args) throws InterruptedException {
    final ExchangeClient client = DirectExchangeClient.create(options(), EXCHANGE);

    client.subscribe("test", LogDetail.class, ReceiveBatchJob::handleWithReply);
    Thread.currentThread().join();
  }

  private static Reply<LogDetail> handleWithReply(final LogDetail msg,
      final MessageContext context) {
    final LogDetail reply = new LogDetail("Reply Message > " + System.nanoTime(), "WARN");
    log.info("Processing message : {} , {}", msg, context);

    return Reply.with(reply);
  }

}
```
## Error handling

Error handling is build into the client directly. When new subscription is created an error exchange and
queue are defined for tracking processing errors and can be accessed from the returned `Subscriptoin`

```java
    final SubscriptionResult subscription = client
        .subscribe("test", LogDetail.class, ReceiveBatchJob::handleWithReply);

    log.info("Subscription : {}", subscription);
```

```java
SubscriptionResult
{
    exchange='exchange.batch', 
    queue='test:exchange.batch:examples.LogDetail', 
    errorExchangeName='hoplin.error.exchange',
    errorQueueName='test:exchange.batch:examples.LogDetail.error'
}
```

Messages are routed to default error exchange : `hoplin.error.exchange`

Sample `MessageError` with original payload.
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
Hoplin does not have any dependencies on any existing metrics libraries but rather it provides a way 
to hook into the underlying metrics via `MetricsPublisher` interface. 
Metrics expose a number of key/value pairs that are updated and send to metrics consumers. 

Depending on which client we use the metrics key will be different and it is up to the consumer to normalize the name. 
Data is packed into a `Map<String, Map<String,String>>` structure, this allows us to add metrics easily without breaking any API.

**Sample Payload**

```text
 {exchange.rpc.logs-rpc.request.log={received.size=211, sent.size=204, received.count=1, sent.count=1}}
``` 
 
```text
Metrics Key      = exchange.rpc.logs-rpc.request.log 
received.size    = Amount of data received by this client
received.count   = Number of messages received

sent.size        = Amount of data sent by this client
sent.count       = Number of messages sent
``` 

**Instantiating metrics consumer**
 
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
```json
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
  private final String msg;
  private final String level;

  public LogDetail(final String msg, final String level) {
    this.msg = msg;
    this.level = level;
  }
}
```

## Closing clients
Client can be closed couple ways.
First by calling `close` method on the client itself.

```java
  ExchangeClient client = clientFromExchange();
  // do something here 
  client.close();
```

Second method would to be get the client wrapped in `AutoCloseable` as `CloseableExchangeClient`
and using `try-with-resources` 
```java
    try (CloseableExchangeClient client = clientFromExchange().asClosable()) {
       // do something here  
    }
```
Both of this methods will  invoke the same `close` method. Deciding which method suit best depends
on the use case of how the client is used.

## Notes

Checking number of active rabbitmq connections

Method 1
```bash
    sudo netstat -anp | grep :5672 | grep ESTABLISHED | wc -l
```
Method 2

```bash
rabbitmqctl list_connections
```

`hoplin_default_error_queue`

# Code Style
[Google Style Guides](https://github.com/google/styleguide)

# Resources

[RabbitMQ](https://www.rabbitmq.com/)

[RawRabbit](https://github.com/pardahlman/RawRabbit)

[EasyNetQ](https://github.com/EasyNetQ/EasyNetQ)

OSSRH-43588
