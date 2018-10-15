package hoplin.io;

import java.util.Collections;
import java.util.Objects;

public class BindingBuilder
{
    private BindingBuilder()
    {
        super();
    }

    public static DestinationConfigurer bind(final Queue queue)
    {
        return new DestinationConfigurer(queue);
    }

    public static DestinationConfigurer bind()
    {
        return new DestinationConfigurer(new Queue(""));
    }

    public static class DestinationConfigurer
    {
        private Queue queue;

        public DestinationConfigurer(final Queue queue)
        {
            Objects.requireNonNull(queue);
            this.queue = queue;
        }

        public Binding to(final FanoutExchange exchange)
        {
            return new Binding(queue.getName(),  exchange.getName(), Collections.emptyMap());
        }

        public TopicExchangeRoutingKeyConfigurer to(final TopicExchange exchange)
        {
            return new TopicExchangeRoutingKeyConfigurer(this, exchange);
        }

        public DirectExchangeRoutingKeyConfigurer to(final DirectExchange exchange)
        {
            return new DirectExchangeRoutingKeyConfigurer(this, exchange);
        }
    }

    private abstract static class RoutingKeyConfigurer<E extends Exchange>
    {
        protected final BindingBuilder.DestinationConfigurer destination;

        protected final String exchange;

        public RoutingKeyConfigurer(final BindingBuilder.DestinationConfigurer destination, final String exchange)
        {
            this.destination = destination;
            this.exchange = exchange;
        }
    }

    public static class TopicExchangeRoutingKeyConfigurer extends RoutingKeyConfigurer <TopicExchange>
    {
        private boolean autoAck;

        public TopicExchangeRoutingKeyConfigurer(final DestinationConfigurer configurer, final TopicExchange exchange)
        {
            super(configurer, exchange.getName());
        }

        public TopicExchangeRoutingKeyConfigurer withAutoAck(final boolean autoAck)
        {
            this.autoAck = autoAck;
            return this;
        }
         
        public Binding with(final String routingKey)
        {
           return new Binding(destination.queue.getName(), exchange, routingKey, Collections.emptyMap());
        }

        public Binding with(final Enum<?> routingKey)
        {
           return new Binding(destination.queue.getName(), exchange, routingKey.toString(), Collections.emptyMap());
        }
    }

    public static class DirectExchangeRoutingKeyConfigurer extends RoutingKeyConfigurer <DirectExchange>
    {
        private boolean autoAck;


        public DirectExchangeRoutingKeyConfigurer(final DestinationConfigurer configurer, final DirectExchange exchange)
        {
            super(configurer, exchange.getName());
        }

        public Binding with(final String routingKey)
        {
            return new Binding(destination.queue.getName(), exchange, routingKey, Collections.emptyMap());
        }

        public DirectExchangeRoutingKeyConfigurer withAutoAck(final boolean autoAck)
        {
            this.autoAck = autoAck;
            return this;
        }

        public Binding with(final Enum<?> routingKey)
        {
            return new Binding(destination.queue.getName(), exchange, routingKey.toString(), Collections.emptyMap());
        }

        public Binding withQueueName()
        {
            return new Binding(destination.queue.getName(), exchange, destination.queue.getName(), Collections.emptyMap());
        }
    }
}





