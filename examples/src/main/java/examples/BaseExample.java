package examples;

import io.hoplin.RabbitMQOptions;
import io.hoplin.SubscriptionResult;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseExample {

    private static final Logger log = LoggerFactory.getLogger(BaseExample.class);

    /**
     * Create 'default' connection options
     *
     * @return
     */
    protected static RabbitMQOptions options() {
        final RabbitMQOptions options = new RabbitMQOptions();

        options.setConnectionRetries(25);
        options.setConnectionRetryDelay(500L);

        return options;
    }

    /**
     * Create options from connection string
     *
     * @param connectionString
     * @return
     */
    protected static RabbitMQOptions options(final String connectionString) {
        return RabbitMQOptions.from(connectionString);
    }

    /**
     * Display subscription details
     *
     * @param subscription
     */
    protected static void info(final SubscriptionResult subscription) {
        Objects.requireNonNull(subscription);
        log.info("Subscription Exchange         : {}", subscription.getExchange());
        log.info("Subscription Queue            : {}", subscription.getQueue());
        log.info("Subscription ErrorExchange    : {}", subscription.getErrorExchangeName());
        log.info("Subscription ErrrorQueue      : {}", subscription.getErrorQueueName());
    }

}
