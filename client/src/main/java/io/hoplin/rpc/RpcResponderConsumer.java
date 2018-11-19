package io.hoplin.rpc;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import io.hoplin.HoplinRuntimeException;
import io.hoplin.MessagePayload;
import io.hoplin.json.JsonCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

/**
 * On the reception of each RPC request, this consumer will
 *
 * <ul>
 *     <li>Perform the action required in the RPC request</li>
 *     <li>Prepare the reply message Set the correlation ID in the reply properties</li>
 *     <li>Publish the answer on the reply queue</li>
 *     <li>Send the ack to the RPC request</li>
 * </ul>
 *
 * @param <I>
 * @param <O>
 */
public class RpcResponderConsumer<I, O> extends DefaultConsumer
{
    private static final Logger log = LoggerFactory.getLogger(RpcResponderConsumer.class);

    private final Executor executor;

    private final Function<I, O> handler;

    private JsonCodec codec;

    /**
     * Constructs a new instance and records its association to the passed-in channel.
     *  @param channel the channel to which this consumer is attached
     * @param handler
     * @param executor
     */

    public RpcResponderConsumer(final Channel channel,
                                       final Function<I, O> handler,
                                       final Executor executor)
    {
        super(channel);

        this.executor = Objects.requireNonNull(executor);
        this.handler = Objects.requireNonNull(handler);
        this.codec = new JsonCodec();
    }

    @Override
    public void handleDelivery(final String consumerTag,
                               final Envelope envelope,
                               final AMQP.BasicProperties properties,
                               final byte[] body)
    {
        // 1 : Perform the action required in the RPC request
        CompletableFuture
                .supplyAsync(()-> dispatch(body), executor)
                .whenComplete((reply, throwable) ->
        {
            try
            {
                //0 :there was exception while processing message
                if(throwable != null)
                {
                    nack(envelope);
                    return;
                }

                // 2 : Prepare the reply message Set the correlation ID in the reply properties
                final AMQP.BasicProperties replyProperties = new AMQP.BasicProperties
                        .Builder()
                        .correlationId(properties.getCorrelationId())
                        .build();

                // 3 : Publish the answer on the reply queue
                final String replyTo = properties.getReplyTo();

                log.info("replyTo, correlationId :  {}, {}", replyTo, properties.getCorrelationId());

                getChannel().basicPublish("", replyTo, replyProperties, reply);

                // 4 : Send the ack to the RPC request
                getChannel().basicAck(envelope.getDeliveryTag(), false);
            }
            catch (final Exception e)
            {
                log.error("Unable to acknowledgeExceptionally execution", e);
                nack(envelope);
            }
        });
    }


    private void nack(final Envelope envelope)
    {
        try
        {
            final long deliveryTag = envelope.getDeliveryTag();
            if(envelope.isRedeliver())
            {
                sendToDeadMessageQueue(envelope);
            }
            else
            {
                getChannel().basicNack(deliveryTag, false, true);
            }
        }
        catch (final IOException e)
        {
            log.error("unable to NACK : " + envelope, e);
        }
    }

    private void sendToDeadMessageQueue(final Envelope envelope)
    {
        log.warn("marked for DLQ :  {} ", envelope);
        // TODO: send to special queue for internal review
    }

    @SuppressWarnings("unchecked")
    private byte[] dispatch(final byte[] body)
    {
        try
        {
            final MessagePayload<?> requestMsg = codec.deserialize(body, MessagePayload.class);
            final O reply = handler.apply((I) requestMsg.getPayload());
            final MessagePayload<O> replyMsg = new MessagePayload();
            replyMsg.setPayload(reply);

            return codec.serialize(replyMsg, MessagePayload.class);
        }
        catch (final Exception e)
        {
            log.error("Unable to apply reply handler", e);
            throw new HoplinRuntimeException("Unable to apply reply handler", e);
        }
    }
}