package io.hoplin;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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

    public static DestinationConfigurer bind(final String queue)
    {
        return new DestinationConfigurer(new Queue(queue));
    }

    public static class DestinationConfigurer
    {
        private Queue queue;

        public DestinationConfigurer(final Queue queue)
        {
            this.queue = Objects.requireNonNull(queue);
        }

        public Binding to(final FanoutExchange exchange)
        {
            return new Binding(queue.getName(), exchange.getName(), Collections.emptyMap());
        }

        public TopicExchangeRoutingKeyConfigurer to(final TopicExchange exchange)
        {
            return new TopicExchangeRoutingKeyConfigurer(this, exchange);
        }

        public DirectExchangeRoutingKeyConfigurer to(final DirectExchange exchange)
        {
            return new DirectExchangeRoutingKeyConfigurer(this, exchange);
        }

        public HeaderExchangeRoutingKeyConfigurer to(final HeaderExchange exchange)
        {
            return new HeaderExchangeRoutingKeyConfigurer(this, exchange);
        }
    }

    public abstract static class RoutingKeyConfigurer<E extends Exchange, T>
    {
        protected final BindingBuilder.DestinationConfigurer destination;

        protected final String exchange;

        protected boolean autoAck;

        protected int prefetchCount  = 1;

        protected boolean publisherConfirms;

        protected String routingKey;

        public RoutingKeyConfigurer(final BindingBuilder.DestinationConfigurer destination, final String exchange)
        {
            this.destination = destination;
            this.exchange = exchange;
        }

        public T withAutoAck(final boolean autoAck)
        {
            this.autoAck = autoAck;
            return (T) this;
        }

        public T withPrefetchCount(final int prefetchCount)
        {
            this.prefetchCount = prefetchCount;
            return (T) this;
        }

        public T withPublisherConfirms(final boolean publisherConfirms)
        {
            this.publisherConfirms = publisherConfirms;
            return (T) this;
        }

        public T with(final String routingKey)
        {
            this.routingKey = routingKey;
            return (T) this;
        }

        public T with(final Enum<?> routingKey)
        {
            this.routingKey = routingKey.toString();
            return (T) this;
        }

        public abstract Binding build();
    }

    public static class TopicExchangeRoutingKeyConfigurer extends RoutingKeyConfigurer <TopicExchange, TopicExchangeRoutingKeyConfigurer>
    {
        public TopicExchangeRoutingKeyConfigurer(final DestinationConfigurer configurer, final TopicExchange exchange)
        {
            super(configurer, exchange.getName());
        }

        public Binding build()
        {
            return new Binding(destination.queue.getName(), exchange, routingKey, Collections.emptyMap());
        }
    }

    public static class DirectExchangeRoutingKeyConfigurer extends RoutingKeyConfigurer <DirectExchange, DirectExchangeRoutingKeyConfigurer>
    {
        public DirectExchangeRoutingKeyConfigurer(final DestinationConfigurer configurer, final DirectExchange exchange)
        {
            super(configurer, exchange.getName());
        }

        @Override
        public Binding build()
        {
            return new Binding(destination.queue.getName(), exchange, destination.queue.getName(), Collections.emptyMap());
        }
    }

    public static class HeaderExchangeRoutingKeyConfigurer extends RoutingKeyConfigurer <HeaderExchange, HeaderExchangeRoutingKeyConfigurer>
    {
        private Map<String, String> arguments = new HashMap<>();

        public HeaderExchangeRoutingKeyConfigurer(final DestinationConfigurer configurer, final HeaderExchange exchange)
        {
            super(configurer, exchange.getName());
            arguments.put("x-match", "all");
        }

        /**
         * Add binding arguments
         * x-match property  can have 2 values: "any" or "all"
         *
         * @param key the key to add
         * @param value the value to add
         * @return
         */
        public HeaderExchangeRoutingKeyConfigurer arg(final String key, final String value)
        {
            if("x-match".equalsIgnoreCase(key))
            {
                if(!("any".equalsIgnoreCase(value) && "all".equalsIgnoreCase(value)))
                    throw new IllegalArgumentException("x-match property  can have 2 values: \"any\" or \"all\" but got :" +value);
            }

            arguments.put(key, value);
            return this;
        }

        /**
         * Add binding arguments
         *
         * @param arguments
         * @return
         */
        public HeaderExchangeRoutingKeyConfigurer args(final Map<String, String> arguments)
        {
            arguments.forEach(this::arg);
            return this;
        }

        @Override
        public Binding build()
        {
            final Map<String, Object> args = arguments.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            return new Binding(destination.queue.getName(), exchange, "", args);
        }
    }
}





