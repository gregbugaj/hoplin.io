package io.hoplin.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Publisher that pushes metrics to specified {@link Consumer} <br/>
 *
 * <strong>Usage example</strong>
 * <blockquote><pre>
 * {@code
 *   FunctionMetricsPublisher
 *           .consumer(RpcServerExample::metrics)
 *           .withInterval(1, TimeUnit.SECONDS)
 *           .withResetOnReporting(false)
 *           .build()
 *           .start();
 * }
 * </pre></blockquote>
 */
public class FunctionMetricsPublisher implements MetricsPublisher
{
    private static final Logger log = LoggerFactory.getLogger(FunctionMetricsPublisher.class);

    private final Consumer consumer;

    private  boolean resetOnReporting;

    private long time;

    private TimeUnit unit;

    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private FunctionMetricsPublisher(Consumer consumer, long time, TimeUnit unit, boolean resetOnReporting)
    {
        this.consumer = consumer;
        this.time = time;
        this.unit = unit;
        this.resetOnReporting = resetOnReporting;
    }

    @Override
    public void start() 
    {
        executor.scheduleAtFixedRate(this::publish,time, time, unit);
    }

    private void publish()
    {
        final Map<String, QueueMetrics> metrics = QueueMetrics.Factory.getMetrics();
        final Map<String, Map<String, String>>  collected = new HashMap<>();

        metrics.forEach((key, value)->{
            final Map<String, String> data = new HashMap<>();
            data.put("received.count", Long.toString(value.getMessageReceived()));
            data.put("received.size", Long.toString(value.getReceivedSize()));
            data.put("sent.count", Long.toString(value.getMessageSent()));
            data.put("sent.size", Long.toString(value.getSentSize()));

            if(resetOnReporting)
                value.reset();

            collected.put(key, data);
        });

        consumer.accept(collected);
    }

    @Override
    public void shutdown() {
        executor.shutdown();
    }

    public static Builder consumer(final Consumer consumer)
    {
        Objects.requireNonNull(consumer);
        return new Builder(consumer);
    }

    public static class Builder
    {
        private Consumer<Map<String, Map<String, String>>> consumer;

        private long time;

        private TimeUnit unit;

        private boolean resetOnReporting;

        private Builder(final Consumer<Map<String, Map<String, String>>> consumer)
        {
            this.consumer = consumer;
        }

        /**
         * Interval at which metrics will be published
         *
         * @param time
         * @param unit
         * @return
         */
        public Builder withInterval(long time, TimeUnit unit)
        {
            this.time = time;
            this.unit = unit;
            return this;
        }

        /**
         * Should statistics reset when they have been published.
         *
         * @param reset
         * @return
         */
        public Builder withResetOnReporting(boolean reset)
        {
            this.resetOnReporting = reset;
            return this;
        }

        /**
         * Build metrics provider
         * @return
         */
        public MetricsPublisher build()
        {
            return new FunctionMetricsPublisher(consumer, time, unit, resetOnReporting);
        }
    }
}
