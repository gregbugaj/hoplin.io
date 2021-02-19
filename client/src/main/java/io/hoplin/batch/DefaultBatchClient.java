package io.hoplin.batch;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import io.hoplin.Binding;
import io.hoplin.ConnectionProvider;
import io.hoplin.HoplinRuntimeException;
import io.hoplin.MessagePayload;
import io.hoplin.RabbitMQOptions;
import io.hoplin.json.JsonMessagePayloadCodec;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of batching client
 */
public class DefaultBatchClient implements BatchClient {

  private static final Logger log = LoggerFactory.getLogger(DefaultBatchClient.class);

  private final ConnectionProvider provider;
  /**
   * Channel we are communicating on
   */
  private final Channel channel;
  /**
   * Exchange to send requests to
   */
  private final String exchange;
  private final JsonMessagePayloadCodec codec;
  /**
   * Queue where we will listen for our Batch-RPC replies
   */
  private String replyToQueueName;

  private boolean directReply;

  private final ConcurrentHashMap<UUID, CompletableFutureWrapperBatchContext> batches = new ConcurrentHashMap<>();

  private BatchReplyConsumer consumer;

  public DefaultBatchClient(final RabbitMQOptions options, final Binding binding) {
    Objects.requireNonNull(options);
    Objects.requireNonNull(binding);

    this.provider = ConnectionProvider.createAndConnect(options);
    this.channel = provider.acquire();
    this.codec = new JsonMessagePayloadCodec();

    this.exchange = binding.getExchange();
    this.replyToQueueName = binding.getQueue();

    bind();
    consumeReply();
  }

  /**
   * Declare the request queue where the responder will be waiting for the RPC requests Create a
   * temporary, private, autodelete reply queue
   */
  private void bind() {
    if (replyToQueueName == null ||
        replyToQueueName.isEmpty() ||
        "amq.rabbitmq.reply-to".equalsIgnoreCase(replyToQueueName)) {
      replyToQueueName = "amq.rabbitmq.reply-to";
      directReply = true;
    } else {
      replyToQueueName = replyToQueueName + ".reply-to." + UUID.randomUUID();
    }

    log.info("Param Exchange, ReplyTo, directReply  : {}, {}, {}", exchange, replyToQueueName,
        directReply);

    try {
      if (!directReply) {
        channel.exchangeDeclare(exchange, "direct", true, false, null);
        channel.queueDeclare(replyToQueueName, false, false, true, null);
      }
    } catch (final Exception e) {
      throw new HoplinRuntimeException("Unable to bind queue", e);
    }
  }

  @Override
  public CompletableFuture<BatchContext> startNew(final Consumer<BatchContext> consumer) {
    Objects.requireNonNull(consumer);

    final CompletableFuture<BatchContext> future = new CompletableFuture<>();
    final BatchContext context = new BatchContext();
    final UUID batchId = context.getBatchId();
    batches.put(batchId, new CompletableFutureWrapperBatchContext(future, context));

    consumer.accept(context);

    final List<BatchContextTask> tasks = context.getSubmittedTasks();
    int total = tasks.size();
    final AtomicLong index = new AtomicLong();

    for (final BatchContextTask task : tasks) {
      basicPublish(batchId, task, "");
      final UUID taskId = task.getTaskId();
      final long id = index.incrementAndGet();
      log.info("Batch task added [{} of {}]: {} : {}", index, total, taskId, task);
    }
    return future;
  }

  @Override
  public CompletableFuture<BatchContext> startNew(final Iterable<Supplier<?>> tasksToAdd) {
    Objects.requireNonNull(tasksToAdd);
    return startNew(context -> tasksToAdd.forEach(context::enqueue));
  }

  /**
   * @param batchId
   * @param request
   * @param routingKey
   * @return
   */
  private void basicPublish(final UUID batchId, final BatchContextTask request,
      final String routingKey) {
    if (routingKey == null) {
      throw new IllegalArgumentException("routingKey should not be null");
    }

    try {
      if (log.isDebugEnabled()) {
        log.debug("Publishing to Exchange = {}, RoutingKey = {} , ReplyTo = {}", exchange,
            routingKey, replyToQueueName);
      }

      final UUID taskId = request.getTaskId();
      final Map<String, Object> headers = new HashMap<>();
      headers.put("x-batch-id", batchId.toString());

      final AMQP.BasicProperties props = new AMQP.BasicProperties
          .Builder()
          .correlationId(taskId.toString())
          .replyTo(replyToQueueName)
          .headers(headers)
          .build();

      channel.basicPublish(exchange, routingKey, props, createRequestPayload(request.getMessage()));
    } catch (final IOException e) {
      log.error("Unable to send request", e);
    }
  }

  @Override
  public UUID continueWith(final UUID batchId, final Consumer<BatchContext> context) {
    throw new RuntimeException("Not yet implemented");
  }

  @Override
  public void cancel(final UUID batchId) {
    throw new RuntimeException("Not yet implemented");
  }

  private <I> byte[] createRequestPayload(final I request) {
    return codec.serialize(new MessagePayload<>(request));
  }

  private void consumeReply() {
    try {
      consumer = new BatchReplyConsumer(channel, batches);
      channel.basicConsume(replyToQueueName, true, consumer);
    } catch (final Exception e) {
      throw new HoplinRuntimeException("Unable to create batch consumer", e);
    }
  }

  @Override
  public void close() {
    if (provider != null) {
      try {
        provider.disconnect();
      } catch (IOException e) {
        log.warn("Error during close", e);
      }
    }
  }
}
