package io.hoplin.rpc;

import io.hoplin.Binding;
import io.hoplin.RabbitMQOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * RPC Server/Client
 */
public class Rpc
{
    private static final Logger log = LoggerFactory.getLogger(Rpc.class);

    /**
     * Create new {@link RpcServer}
     *
     * @param options the connection options to use
     * @param binding the {@link Binding} to use
     * @return new Direct Exchange client setup in server mode
     */
    public static RpcServer server(final RabbitMQOptions options, final Binding binding)
    {
        Objects.requireNonNull(options);
        Objects.requireNonNull(binding);

        return new RpcServer(options, binding);
    }

    public static <T, K> RpcClient<T, K> client(final RabbitMQOptions options, final  Binding binding)
    {
        Objects.requireNonNull(options);
        Objects.requireNonNull(binding);

        return new RpcClient<>(options, binding);
    }
}
