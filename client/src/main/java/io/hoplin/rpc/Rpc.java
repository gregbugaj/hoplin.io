package io.hoplin.rpc;

import io.hoplin.Binding;
import io.hoplin.RabbitMQOptions;

public class Rpc
{
    /**
     * Create new RPC client
     *
     * @param options the connection options
     * @param binding the binding to use
     * @return
     */
    static RpcClient client(final RabbitMQOptions options, final Binding binding)
    {
        return DefaultRpcClient.create(options, binding);
    }

    /**
     * Create new RPC server
     *
     * @param options the connection options
     * @param binding the binding to use
     * @return
     */
    static RpcServer server(final RabbitMQOptions options, final Binding binding)
    {
        return DefaultRpcServer.create(options, binding);
    }

    /**
     * Create new RPC client/server
     *
     * @param options the connection options
     * @param binding the binding to use
     * @return
     */
    static RpClientServer clientServer(final RabbitMQOptions options, final Binding binding)
    {
        return new DefaultRpClientServer(options, binding);
    }
}
