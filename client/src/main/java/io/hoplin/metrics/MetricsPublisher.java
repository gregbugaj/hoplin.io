package io.hoplin.metrics;

public interface MetricsPublisher
{
    /**
     * Start streaming results to the publisher
     */
    void start();

    /**
     * Initialize gracefull shutdown
     */
    void shutdown();
}
