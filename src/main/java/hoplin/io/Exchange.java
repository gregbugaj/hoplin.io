package hoplin.io;

import java.util.Map;

/**
 * https://www.rabbitmq.com/tutorials/amqp-concepts.html
 * There are four built-in exchange types in AMQP
 *
 *<ul>
 *     <li>Direct</li>
 *     <li>Fanout</li>
 *     <li>Topic</li>
 *     <li>Headers</li>
 *</ul>
 * each exchange type has its own routing semantics
 */
public interface Exchange
{
    /**
     * Exchange attributes
     *
     * @return
     */
    Map<String, Object> getArguments();

    /**
     * The name of the exchange.
     *
     * @return the name
     */
    String	getName();

    /**
     * True if the server should delete the exchange when it is no longer in use (if all bindings are deleted)
     *
     * @return {@code true} if auto-delete
     */
    boolean isAutoDelete();

    /**
     * A durable exchange will survive a server restart
     *
     * @return {@code true} if the durable
     */
    boolean isDurable();

    /**
     * The type of the exchange.
     *
     * @return ExchangeType
     */
    ExchangeType getType();

}
