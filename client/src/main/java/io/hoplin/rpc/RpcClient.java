package io.hoplin.rpc;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Rpc Client interface
 *
 * @param <I>
 * @param <O>
 */
public interface RpcClient<I, O> {

  /**
   * Submit request and await response
   *
   * @param request the request to submit
   * @return reply from the server
   */
  O request(final I request);

  /**
   * Submit request and await response
   *
   * @param request    the request to submit
   * @param routingKey Routing key to use for requests
   * @return reply from the server
   */
  O request(final I request, final String routingKey);

  /**
   * Submit request and await response
   *
   * @param request the request to submit
   * @param timeout the time of how long to wait for the response
   * @return reply from the server
   */

  O request(final I request, final Duration timeout);

  /**
   * Submit request and await response
   *
   * @param request    the request to submit
   * @param routingKey Routing key to use for requests
   * @param timeout    the time of how long to wait for the response
   * @return reply from the server
   */
  O request(final I request, final String routingKey, final Duration timeout);

  /**
   * Submit request and return completable future
   *
   * @param request the request to submit
   * @return CompletableFuture
   */
  CompletableFuture<O> requestAsync(final I request);

  /**
   * Submit request and return completable future
   *
   * @param request    the request to submit
   * @param routingKey Routing key to use for requests
   * @return CompletableFuture
   */
  CompletableFuture<O> requestAsync(final I request, final String routingKey);

  /**
   * Submit request and return completable future
   *
   * @param request the request to submit
   * @param timeout the time of how long to wait for the response
   * @return CompletableFuture
   */
  CompletableFuture<O> requestAsync(final I request, final Duration timeout);

  /**
   * Submit request and return completable future
   *
   * @param request    the request to submit
   * @param routingKey Routing key to use for requests
   * @param timeout    the time of how long to wait for the response
   * @return CompletableFuture
   */
  CompletableFuture<O> requestAsync(final I request, final String routingKey,
      final Duration timeout);

}
