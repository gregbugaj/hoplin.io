package io.hoplin.rpc;

import io.hoplin.Binding;
import io.hoplin.RabbitMQOptions;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Default Client/Server
 *
 * @param <I>
 * @param <O>
 */
public class DefaultRpClientServer<I, O> implements RpClientServer<I, O> {

  private final RpcClient<I, O> client;

  private final RpcServer<I, O> server;

  @SuppressWarnings("unchecked")
  public DefaultRpClientServer(final RabbitMQOptions options, final Binding binding) {
    Objects.requireNonNull(options);
    Objects.requireNonNull(binding);

    client = Rpc.client(options, binding);
    server = Rpc.server(options, binding);
  }

  @Override
  public O request(I request) {
    return client.request(request);
  }

  @Override
  public O request(I request, String routingKey) {
    return client.request(request, routingKey);
  }

  @Override
  public O request(I request, Duration timeout) {
    return client.request(request, timeout);
  }

  @Override
  public O request(I request, String routingKey, Duration timeout) {
    return client.request(request, routingKey, timeout);
  }

  @Override
  public CompletableFuture<O> requestAsync(I request) {
    return client.requestAsync(request);
  }

  @Override
  public CompletableFuture<O> requestAsync(I request, String routingKey) {
    return client.requestAsync(request, routingKey);
  }

  @Override
  public CompletableFuture<O> requestAsync(I request, Duration timeout) {
    return client.requestAsync(request, timeout);
  }

  @Override
  public CompletableFuture<O> requestAsync(I request, String routingKey, Duration timeout) {
    return client.requestAsync(request, routingKey, timeout);
  }

  @Override
  public void respondAsync(final Function<I, O> handler) {
    server.respondAsync(handler);
  }
}
