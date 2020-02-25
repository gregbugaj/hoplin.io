package io.hoplin;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Envelope;
import java.util.Objects;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Context that message is associated with Queue name will be populated when it is known
 */
public class MessageContext {

  // Message body
  private byte[] body;

  private MessageReceivedInfo receivedInfo;

  private AMQP.BasicProperties properties;

  private JobExecutionInformation executionInfo;

  /**
   * Create new message context
   *
   * @param queue
   * @param consumerTag
   * @param envelope
   * @param properties
   * @return newly created {@link MessageContext}
   */
  public static MessageContext create(final String queue, final String consumerTag,
      final Envelope envelope,
      BasicProperties properties, byte[] body) {
    Objects.requireNonNull(consumerTag);
    Objects.requireNonNull(envelope);

    final MessageReceivedInfo receivedInfo = new MessageReceivedInfo(consumerTag,
        envelope.getDeliveryTag(),
        envelope.isRedeliver(),
        envelope.getExchange(),
        envelope.getRoutingKey(),
        queue,
        System.currentTimeMillis()
    );

    final MessageContext context = new MessageContext();
    context.setBody(body);
    context.setReceivedInfo(receivedInfo);
    context.setProperties(properties);

    return context;
  }

  /**
   * Create new message context
   *
   * @param envelope
   * @param properties
   * @param body
   * @return newly created {@link MessageContext}
   */
  public static MessageContext create(final String consumerTag,
      final Envelope envelope,
      BasicProperties properties, byte[] body) {

    return create("", consumerTag, envelope, properties, body);
  }

  public MessageReceivedInfo getReceivedInfo() {
    return receivedInfo;
  }

  public MessageContext setReceivedInfo(final MessageReceivedInfo receivedInfo) {
    this.receivedInfo = receivedInfo;
    return this;
  }

  public AMQP.BasicProperties getProperties() {
    return properties;
  }

  public MessageContext setProperties(final AMQP.BasicProperties properties) {
    this.properties = properties;
    return this;
  }

  public byte[] getBody() {
    return body;
  }

  public void setBody(byte[] body) {
    this.body = body;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }


  public JobExecutionInformation getExecutionInfo() {
    return executionInfo;
  }

  public void setExecutionInfo(JobExecutionInformation executionInfo) {
    this.executionInfo = Objects.requireNonNull(executionInfo);
  }
}
