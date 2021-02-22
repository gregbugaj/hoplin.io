package io.hoplin;

import com.google.common.collect.ArrayListMultimap;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import io.hoplin.json.JsonMessagePayloadCodec;
import io.hoplin.metrics.QueueMetrics;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default queue consumer
 */
public class DefaultQueueConsumer extends DefaultConsumer {

  private static final Logger log = LoggerFactory.getLogger(DefaultQueueConsumer.class);

  private final String queue;

  private final QueueOptions queueOptions;

  private final QueueMetrics metrics;

  private final ConsumerErrorStrategy errorStrategy;

  private final ArrayListMultimap<Class<?>, MethodReference<?>> handlers = ArrayListMultimap
      .create();

  private final Publisher publisher;

  private final ExecutorService executor;

  /**
   * Constructs a new instance and records its association to the passed-in channel.
   *
   * @param queue
   * @param channel      the channel to which this consumer is attached
   * @param queueOptions the options to use for this queue consumer
  public DefaultQueueConsumer(String queue, final Channel channel,
  final QueueOptions queueOptions) {
  this(queue, channel, queueOptions,
  Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
  }
   */

  /**
   * Construct a new instance of queue consumer
   *
   * @param channel      the channel bound to this consumer
   * @param queueOptions the options for this consumer
   * @param executor     the {@link ExecutorService} to use for this consumer
   */
  public DefaultQueueConsumer(final String queue, final Channel channel,
      final QueueOptions queueOptions, final ExecutorService executor) {
    super(channel);
    this.queueOptions = Objects.requireNonNull(queueOptions);
    this.executor = Objects.requireNonNull(executor);
    this.queue = queue;
    this.errorStrategy = new DefaultConsumerErrorStrategy(channel);
    this.metrics = QueueMetrics.Factory.getInstance(queue);
    this.publisher = new Publisher(executor);
  }

  /**
   * If you don't send the ack back, the consumer continues to fetch subsequent messages; however,
   * when you disconnect the consumer, all the messages will still be in the queue. Messages are not
   * consumed until RabbitMQ receives the corresponding ack.
   * <p>
   * Note: A message must be acknowledged only once;
   * </p>
   */
  @SuppressWarnings("unchecked")
  @Override
  public void handleDelivery(final String consumerTag, final Envelope envelope,
      final AMQP.BasicProperties properties, byte[] body) {

    metrics.markMessageReceived();
    metrics.incrementReceived(body.length);

    final MessageContext context = MessageContext
        .create(queue, consumerTag, envelope, properties, body);

    CompletableFuture.runAsync(() -> {
      AckStrategy ack;

      try {
        ack = ackFromOptions(queueOptions);

        final JsonMessagePayloadCodec codec = new JsonMessagePayloadCodec(handlers.keySet());
        final MessagePayload<?> message = codec.deserialize(body, MessagePayload.class);
        final Object val = message.getPayload();
        final Class<?> targetClass = message.getTypeAsClass();
        final Collection<MethodReference<?>> consumers = handlers.get(targetClass);
        final List<Throwable> exceptions = new ArrayList<>();
        int invokedHandlers = 0;
        final boolean batchRequest = isBatchedRequest(context);

        Reply<?> reply = null;
        for (final MethodReference reference : consumers) {
          reply = null;
          try {
            final BiFunction<Object, MessageContext, Reply<?>> handler = reference.getHandler();
            final Class<?> root = reference.getRootType();
            if (root == targetClass) {
              ++invokedHandlers;
              reply = execute(context, val, handler);
            } else { // Down cast if necessary
              final Optional<?> castedValue = safeCast(val, targetClass);
              if (castedValue.isPresent()) {
                ++invokedHandlers;
                reply = execute(context, castedValue.get(), handler);
              }
            }
            if (log.isDebugEnabled()) {
              log.debug("reply : {}", reply);
            }

            // can't have multiple handlers for batched requests
            if (batchRequest) {
              break;
            }
          } catch (final Exception e) {
            exceptions.add(e);
            log.error("Handler error for message  : " + message, e);
          }
        }

        // TODO : This should be handled better
        if (invokedHandlers == 0) {
          throw new HoplinRuntimeException("No handlers defined for type : " + targetClass);
        }

        if (batchRequest) {
          final String replyTo = properties.getReplyTo();
          final String correlationId = properties.getCorrelationId();
          final Map<String, Object> headers = properties.getHeaders();
          final Object batchId = headers.get("x-batch-id");
          headers.put("x-batch-correlationId", correlationId);

          if (log.isDebugEnabled()) {
            log.debug("BatchIncoming context        >  {}", context);
            log.debug("BatchIncoming replyTo        >  {}", replyTo);
            log.debug("BatchIncoming correlationId  >  {}", correlationId);
            log.debug("BatchIncoming batchId        >  {}", batchId);
          }

          final JobExecutionInformation executionInfo = context.getExecutionInfo();
          if (log.isDebugEnabled()) {
            log.debug("Handler time : {}", executionInfo.asElapsedMillis());
          }

          if (reply != null && !reply.isExceptional()) {
            publisher.basicPublish(getChannel(), "", replyTo, reply.getValue(), headers);
            ack = AcknowledgmentStrategies.BASIC_ACK.strategy();
          }
        }

        if (reply != null && reply.isExceptional()) {
          ack = errorStrategy.handleConsumerError(context, reply.getException());
        }
      } catch (final Exception e) {
        log.error("Unable to process message", e);
        try {
          ack = errorStrategy.handleConsumerError(context, e);
        } catch (final Exception ex2) {
          log.error("Exception in error strategy", ex2);
          ack = AcknowledgmentStrategies.BASIC_ACK.strategy();
        }
      }

      AckStrategy.acknowledge(getChannel(), context, ack);
    }, executor);
  }

  /**
   * Check if this is a batch request
   *
   * @param context
   * @return
   */
  private boolean isBatchedRequest(MessageContext context) {
    final AMQP.BasicProperties properties = context.getProperties();
    final Map<String, Object> headers = properties.getHeaders();
    if (headers != null) {
      return headers.get("x-batch-id") != null;
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  private Reply<?> execute(final MessageContext context, final Object val,
      final BiFunction<Object, MessageContext, Reply<?>> handler) {
    final JobExecutionInformation exec = new JobExecutionInformation();
    context.setExecutionInfo(exec);
    exec.setStartTime(System.nanoTime());
    try {
      return handler.apply(val, context);
    } catch (final Exception e) {
      return Reply.exceptionally(e);
    } finally {
      exec.setEndTime(System.nanoTime());
    }
  }

  private <S, T> Optional<T> safeCast(final S candidate, Class<T> targetClass) {
    return targetClass.isInstance(candidate)
        ? Optional.of(targetClass.cast(candidate))
        : Optional.empty();
  }

  private AckStrategy ackFromOptions(final QueueOptions queueOptions) {
    if (queueOptions.isAutoAck()) {
      return AcknowledgmentStrategies.NOOP.strategy();
    }
    return AcknowledgmentStrategies.BASIC_ACK.strategy();
  }

  /**
   * Add new handler bound to a specific type
   *
   * @param clazz
   * @param handler
   * @param <T>
   */
  public synchronized <T> void addHandler(final Class<T> clazz,
      final BiFunction<T, MessageContext, Reply<?>> handler) {
    Objects.requireNonNull(clazz);
    Objects.requireNonNull(handler);
    Class<? super T> clz = clazz;

    while (clz != Object.class) {
      handlers.put(clz, MethodReference.of(clazz, handler));
      clz = clz.getSuperclass();
    }

    log.info("Adding handlers : {}", handlers);
  }

  @Override
  public void handleCancel(final String consumerTag) {
    // TODO : consumer has been cancelled unexpectedly
    throw new HoplinRuntimeException("Not yet implemented");
  }

  private static class MethodReference<T> {

    private final Class<T> root;

    private final BiFunction<T, MessageContext, Reply<?>> handler;

    public MethodReference(final Class<T> root,
        final BiFunction<T, MessageContext, Reply<?>> handler) {
      this.root = Objects.requireNonNull(root);
      this.handler = Objects.requireNonNull(handler);
    }

    /**
     * Create new method reference for a specific handler
     *
     * @param root
     * @param handler
     * @param <T>
     * @return
     */
    public static <T> MethodReference<T> of(final Class<T> root,
        final BiFunction<T, MessageContext, Reply<?>> handler) {
      return new MethodReference<T>(root, handler);
    }

    /**
     * Get the root type for the handler
     *
     * @return
     */
    public Class<T> getRootType() {
      return root;
    }

    public BiFunction<T, MessageContext, Reply<?>> getHandler() {
      return handler;
    }

    @Override
    public String toString() {
      return root + ":" + handler;
    }
  }
}
