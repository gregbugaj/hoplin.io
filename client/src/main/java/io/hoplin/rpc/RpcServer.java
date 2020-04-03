package io.hoplin.rpc;

import java.util.function.Function;

public interface RpcServer<I, O> {

  /**
   * Handler responsible for handling incoming requests
   *
   * @param handler
   */
  void respondAsync(final Function<I, O> handler);

  /**
   * Closes associated connections, handlers, executors
   */
  void close();
}
