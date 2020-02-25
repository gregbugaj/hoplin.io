package io.hoplin;

import com.rabbitmq.client.AMQP.BasicProperties;

/**
 * Encapsulated error information for error reprocessing
 */
public class ErrorMessage {

  private String exchange;

  private String queue;

  private String routingKey;

  private long creationTime;

  private String exception;

  // Original message body
  private String body;

  public BasicProperties getProperties() {
    return properties;
  }

  public ErrorMessage setProperties(BasicProperties properties) {
    this.properties = properties;
    return this;
  }

  private BasicProperties properties;

  public String getExchange() {
    return exchange;
  }

  public ErrorMessage setExchange(String exchange) {
    this.exchange = exchange;
    return this;
  }

  public String getQueue() {
    return queue;
  }

  public ErrorMessage setQueue(String queue) {
    this.queue = queue;
    return this;
  }

  public String getRoutingKey() {
    return routingKey;
  }

  public ErrorMessage setRoutingKey(String routingKey) {
    this.routingKey = routingKey;
    return this;
  }

  public long getCreationTime() {
    return creationTime;
  }

  public ErrorMessage setCreationTime(long creationTime) {
    this.creationTime = creationTime;
    return this;
  }

  public String getException() {
    return exception;
  }

  public ErrorMessage setException(String exception) {
    this.exception = exception;
    return this;
  }

  public String getBody() {
    return body;
  }

  public ErrorMessage setBody(String body) {
    this.body = body;
    return this;
  }
}
