package io.hoplin.rpc;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import io.hoplin.Binding;
import io.hoplin.ConnectionProvider;
import io.hoplin.HoplinRuntimeException;
import io.hoplin.QueueOptions;
import io.hoplin.RabbitMQOptions;
import io.hoplin.metrics.QueueMetrics;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of RPC server
 *
 * @param <I> the request type
 * @param <O> the response type
 */
public class DefaultRpcServer<I, O> implements RpcServer<I, O> {

  private static final Logger log = LoggerFactory.getLogger(DefaultRpcServer.class);

  private final ConnectionProvider provider;

  private final QueueMetrics metrics;
  /**
   * Exchange to send requests to
   */
  private final String exchange;

  /**
   * Channel we are communicating on Upon disconnect this channel will be reinitialized
   **/
  private Channel channel;
  /**
   * Routing key to use for requests
   */
  private String routingKey;
  /**
   * Queue name used for incoming request
   */
  private final String requestQueueName;

  // executor that will process incoming RPC requests
  private final ExecutorService executor;

  private Function<I, O> handler;

  private final Binding binding;

  public DefaultRpcServer(final RabbitMQOptions options, final Binding binding,
      final ExecutorService executor) {
    Objects.requireNonNull(options);
    Objects.requireNonNull(binding);
    Objects.requireNonNull(executor);

    this.provider = ConnectionProvider.createAndConnect(options);
    this.executor = executor;
    this.channel = provider.acquire();
    this.exchange = binding.getExchange();
    this.routingKey = binding.getRoutingKey();
    this.requestQueueName = binding.getQueue();

    this.binding = binding;
    if (routingKey == null) {
      routingKey = "";
    }

    this.metrics = QueueMetrics.Factory.getInstance(exchange + "-" + requestQueueName);
    setupChannel();
    bind();
  }

  /**
   * Create new {@link DefaultRpcServer}
   *
   * @param <I>
   * @param <O>
   * @param options the connection options to use
   * @param binding the binding to use
   * @return new Direct Exchange client setup in server mode
   */
  public static <I, O> RpcServer<I, O> create(final RabbitMQOptions options,
      final Binding binding) {
    return new DefaultRpcServer<>(options, binding, createExecutor());
  }

  private static ExecutorService createExecutor() {
    return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
  }

  private void setupChannel() {
    channel.addShutdownListener(sse ->
    {
      log.info("Channel Shutdown, reacquiring : channel #{}", channel.getChannelNumber());
      channel = provider.acquire();

      if (channel != null) {
        log.info("New channel #{}, open = {}", channel, channel.isOpen());
        reInitHandler();
      }
    });
  }

  /**
   * Declare the request queue where the responder will be waiting for the RPC requests Create a
   * temporary, private, autodelete reply queue
   */
  private void bind() {
    log.info("Param RoutingKey  : {}", routingKey);
    log.info("Param Exchange    : {}", exchange);
    log.info("Param Request     : {}", requestQueueName);

    try {
      channel.exchangeDeclare(exchange, "direct", false, true, null);
      channel.queueDeclare(requestQueueName, false, false, true, null);
    } catch (final Exception e) {
      throw new HoplinRuntimeException("Unable to bind queue", e);
    }
  }

  /**
   * Setup consumer in 'server' mode
   *
   * @param handler
   */
  @SuppressWarnings("unchecked")
  private void consumeRequest(final Function<I, O> handler) {
    try {
      final AMQP.Queue.BindOk bindStatus = channel
          .queueBind(requestQueueName, exchange, routingKey);
      log.info("consumeRequest requestQueueName : {}, {}", requestQueueName, bindStatus);

      int prefetchSize = 1;
      if (binding.getOptions() != null) {
        final QueueOptions options = binding.getOptions();
        prefetchSize = options.getPrefetchCount();
      }
      // QOS prefetch is set to unlimited (0 or -1) so we are capping it at the core pool size
      if (prefetchSize < 1) {
        if (executor instanceof ThreadPoolExecutor) {
          prefetchSize = ((ThreadPoolExecutor) executor).getCorePoolSize();
        } else {
          prefetchSize = 1;
        }
      }

      log.info("QOS prefetch set to : {}", prefetchSize);
      channel.basicQos(prefetchSize);
      channel.basicConsume(requestQueueName, false,
          new RpcResponderConsumer(channel, handler, executor, metrics));
    } catch (final Exception e) {
      throw new HoplinRuntimeException("Unable to start RPC server consumer", e);
    }
  }

  private void reInitHandler() {
    log.info("Reinitializing topology & handler");

    setupChannel();
    bind();

    if (handler != null) {
      consumeRequest(handler);
    }
  }

  @Override
  public void respondAsync(final Function<I, O> handler) {
    this.handler = handler;
    consumeRequest(handler);
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
