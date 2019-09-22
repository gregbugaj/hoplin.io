package io.hoplin.rpc;

import io.hoplin.Binding;
import io.hoplin.RabbitMQOptions;
import java.util.concurrent.ExecutorService;

public class Rpc {

  /**
   * Create new RPC client
   *
   * @param options  the connection options
   * @param binding  the binding to use
   * @param executor the executor to use
   * @return
   */
  static RpcClient client(final RabbitMQOptions options, final Binding binding,
      final ExecutorService executor) {
    return DefaultRpcClient.create(options, binding, executor);
  }

  /**
   * Create new RPC server
   *
   * @param options the connection options
   * @param binding the binding to use
   * @return
   */
  static RpcServer server(final RabbitMQOptions options, final Binding binding,
      final ExecutorService executor) {
    return DefaultRpcServer.create(options, binding, executor);
  }

  /**
   * Create new RPC client/server
   *
   * @param options the connection options
   * @param binding the binding to use
   * @return
   */
  static RpClientServer clientServer(final RabbitMQOptions options, final Binding binding,
      final ExecutorService executor) {
    return new DefaultRpClientServer(options, binding, executor);
  }
}
