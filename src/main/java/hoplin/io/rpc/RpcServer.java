package hoplin.io.rpc;

import com.rabbitmq.client.Channel;
import hoplin.io.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * RPC server
 * @param <I>
 * @param <O>
 */
public class RpcServer<I, O>
{
    private static final Logger log = LoggerFactory.getLogger(RpcServer.class);

    private final DefaultRabbitMQClient client;

    private final Binding binding;

    private static final String RPC_QUEUE_NAME = "rpc_queue";

    // executor that will handle incoming RPC requests
    private Executor executor;

    public RpcServer(final RabbitMQOptions options, final Binding binding)
    {
        Objects.requireNonNull(options);
        Objects.requireNonNull(binding);

        this.client = RabbitMQClient.create(options);
        this.binding = binding;
        this.executor  = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        bind();
    }

    /**
     * Declare the request queue where the responder will be waiting for the RPC requests
     * Create a temporary, private, autodelete reply queue
     */
    private void bind()
    {
        try
        {
            final Channel channel = client.channel();
            channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);
            channel.queuePurge(RPC_QUEUE_NAME);
        }
        catch (final Exception e)
        {
            throw new HoplinRuntimeException("Unable to bind queue", e);
        }
    }

    public void start(final Function<I, O> handler)
    {
        try
        {
            log.info("Awaiting RPC requests");
            final Channel channel = client.channel();

            channel.basicQos(1);
            channel.basicConsume(RPC_QUEUE_NAME, false, new RpcResponderConsumer(channel, handler, executor));
        }
        catch (final Exception e)
        {
            throw new HoplinRuntimeException("Unable to start RPC server consumer", e);
        }
    }

    public void disconnect() throws IOException
    {
        if(client != null)
            client.disconnect();
    }
}
