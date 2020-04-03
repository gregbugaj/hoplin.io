package io.hoplin;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Exchange client wrapper that implements {@link AutoCloseable} thus allowing use of
 * try-with-resources pattern
 */
public class DefaultClosableExchangeClient implements CloseableExchangeClient {

  private final ExchangeClient delegate;

  public DefaultClosableExchangeClient(final ExchangeClient client) {
    this.delegate = Objects.requireNonNull(client);
  }

  @Override
  public RabbitMQClient getMqClient() {
    return delegate.getMqClient();
  }

  @Override
  public <T> void publish(T message) {
    delegate.publish(message);
  }

  @Override
  public <T> void publish(T message, String routingKey) {
    delegate.publish(message, routingKey);
  }

  @Override
  public <T> void publish(T message, Consumer<MessageConfiguration> cfg) {
    delegate.publish(message, cfg);
  }

  @Override
  public <T> void publish(T message, String routingKey,
      Consumer<MessageConfiguration> cfg) {
    delegate.publish(message, routingKey, cfg);
  }

  @Override
  public <T> CompletableFuture<Void> publishAsync(T message) {
    return delegate.publishAsync(message);
  }

  @Override
  public <T> CompletableFuture<Void> publishAsync(T message, String routingKey) {
    return delegate.publishAsync(message, routingKey);
  }

  @Override
  public <T> CompletableFuture<Void> publishAsync(T message,
      Consumer<MessageConfiguration> cfg) {
    return delegate.publishAsync(message, cfg);
  }

  @Override
  public <T> CompletableFuture<Void> publishAsync(T message, String routingKey,
      Consumer<MessageConfiguration> cfg) {
    return delegate.publishAsync(message, routingKey, cfg);
  }

  @Override
  public <T> SubscriptionResult subscribe(String subscriberId, Class<T> clazz,
      Consumer<T> handler) {
    return delegate.subscribe(subscriberId, clazz, handler);
  }

  @Override
  public <T> SubscriptionResult subscribe(String subscriberId, Class<T> clazz,
      BiConsumer<T, MessageContext> handler) {
    return delegate.subscribe(subscriberId, clazz, handler);
  }

  @Override
  public <T> SubscriptionResult subscribe(String subscriberId, Class<T> clazz,
      Function<T, Reply<?>> handler) {
    return delegate.subscribe(subscriberId, clazz, handler);
  }

  @Override
  public <T> SubscriptionResult subscribe(String subscriberId, Class<T> clazz,
      BiFunction<T, MessageContext, Reply<?>> handler) {
    return delegate.subscribe(subscriberId, clazz, handler);
  }

  @Override
  public <T> SubscriptionResult subscribe(Class<T> clazz,
      BiFunction<T, MessageContext, Reply<?>> handler,
      Consumer<SubscriptionConfigurator> config) {
    return delegate.subscribe(clazz, handler, config);
  }

  @Override
  public <T> SubscriptionResult subscribe(Class<T> clazz, Consumer<T> handler,
      Consumer<SubscriptionConfigurator> config) {
    return delegate.subscribe(clazz, handler, config);
  }

  @Override
  public void awaitQuiescence() {
    delegate.awaitQuiescence();
  }

  @Override
  public void awaitQuiescence(long time, TimeUnit unit) {
    delegate.awaitQuiescence(time, unit);
  }

  @Override
  public void close() {
    delegate.close();
  }

  @Override
  public CloseableExchangeClient asClosable() {
    throw new HoplinRuntimeException("Client is already wrapped as closable");
  }
}
