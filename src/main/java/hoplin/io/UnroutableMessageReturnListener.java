package hoplin.io;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.ReturnListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 *  Handle unroutable messages
 *
 *  <code>mandatory</code> flag needs to be set while calling channel.basicPublish
 */
public class UnroutableMessageReturnListener implements ReturnListener
{
    private static final Logger log = LoggerFactory.getLogger(UnroutableMessageReturnListener.class);

    @Override
    public void handleReturn(int replyCode,
                      String replyText,
                      String exchange,
                      String routingKey,
                      AMQP.BasicProperties properties,
                      byte[] body)
            throws IOException
    {
        log.warn("Message not delivered : {}, {}", exchange, routingKey);
    }
}
