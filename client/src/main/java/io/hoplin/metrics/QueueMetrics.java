package io.hoplin.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Metrics tracking
 */
public interface QueueMetrics
{
    /**
     * Mark when a messages has been sent
     */
    long markMessageSent();

    /**
     * Get count of message sent
     *
     * @return
     */
    long getMessageSent();

    /**
     * Mark when message has been received
     */
    long markMessageReceived();

    /**
     * Get count of message received
     *
     * @return
     */
    long getMessageReceived();

    /**
     * Increment received total message size by dataSizeInBytes
     * @param dataSizeInBytes the amount to increment by
     */
    void incrementReceived(long dataSizeInBytes);

    /**
     * Increment sent total message size by dataSizeInBytes
     * @param dataSizeInBytes the amount to increment by
     */
    void incrementSend(long dataSizeInBytes);

    /**
     * Get message sent size in bytes
     * @return
     */
    long getSentSize();

    /**
     * Get message received size in bytes
     * @return
     */
    long getReceivedSize();


    class Factory
    {
        private static final Logger log = LoggerFactory.getLogger(QueueMetrics.class);

        private static final Map<String, QueueMetrics> metrics = new ConcurrentHashMap<>();


        public static QueueMetrics getInstance(final String key)
        {
            final QueueMetrics metric = metrics.get(key);
            if (metric != null)
                  return metric;

            // attempt to store
            final QueueMetrics existing = metrics.putIfAbsent(key, new DefaultQueueMetrics());
            if (existing == null)
            {
                // we won the race so retrieve it from  cache
                return metrics.get(key);
            }

            return existing;
        }
    }

    class DefaultQueueMetrics implements QueueMetrics
    {
        private AtomicLong sent = new AtomicLong();

        private AtomicLong received = new AtomicLong();

        private AtomicLong sentData = new AtomicLong();

        private AtomicLong receivedData = new AtomicLong();

        @Override
        public long markMessageSent()
        {
            return sent.incrementAndGet();
        }

        @Override
        public long getMessageSent()
        {
            return sent.get();
        }

        @Override
        public long markMessageReceived()
        {
            return received.incrementAndGet();
        }

        @Override
        public long getMessageReceived()
        {
            return received.get();
        }

        @Override
        public void incrementReceived(long dataSizeInBytes)
        {
            sentData.addAndGet(dataSizeInBytes);
        }

        @Override
        public void incrementSend(long dataSizeInBytes)
        {
            receivedData.addAndGet(dataSizeInBytes);
        }

        @Override
        public long getSentSize()
        {
            return sentData.get();
        }

        @Override
        public long getReceivedSize()
        {
            return receivedData.get();
        }
    }
}
