package examples.rpc;

import examples.BaseExample;
import examples.LogDetail;
import io.hoplin.Binding;
import io.hoplin.BindingBuilder;
import io.hoplin.DirectExchange;
import io.hoplin.rpc.DefaultRpcClient;
import io.hoplin.rpc.RpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * RPC Client example
 */
public class RpcClientExample extends BaseExample
{
    private static final Logger log = LoggerFactory.getLogger(RpcClientExample.class);

    public static void main(final String... args) throws IOException
    {
        final Binding binding = bind();
        log.info("Binding : {}", binding);

        // Blocking
        final RpcClient<LogDetailRequest, LogDetailResponse> client = DefaultRpcClient.create(options(), binding);

        while(true)
        {
            final LogDetailResponse response1 = client.request(new LogDetailRequest("Request message 1", "info"));
            log.info("RPC response : {} ", response1);
            final LogDetailResponse response2 = client.request(new LogDetailRequest("Request message 2", "info"));
            log.info("RPC response : {} ", response2);
        }
    }

    private static void request(RpcClient<LogDetail, String> client)
    {
        long s = System.currentTimeMillis();
        int tickets  = 1;

        for(int i = 0; i < tickets; i++)
        {
            final String reply = client.request(new LogDetail("Msg : " + System.nanoTime(), "info"));
            System.out.println("Reply : " + reply);
        }

        long e = System.currentTimeMillis() - s;
        System.out.println("time : " + e);
    }

    private static void asyncRequest(RpcClient<LogDetail, String> client) throws InterruptedException
    {

        long s = System.currentTimeMillis();
        int tickets  = 1000;
        CountDownLatch latch = new CountDownLatch(tickets);

        for(int i = 0; i < tickets; i++)
        {
            /*final String reply = client.request(new LogDetail("Msg : " + System.nanoTime(), "info"));
            System.out.println("Reply : " + reply);*/
            client
                    .requestAsync(new LogDetail("Msg : " + System.nanoTime(), "info"))
                    .whenComplete((reply, t)->{
                        latch.countDown();
                        System.out.println("Reply : " + reply);
                    });
        }

        latch.await();
        long e = System.currentTimeMillis() - s;
        System.out.printf("time : " + e);
    }

    private static Binding bind()
    {
        // Queue = Name of the queue we will use for 'Reply-To' messages
        // For Direct-Reply leave blank or use 'amq.rabbitmq.reply-to'
        // If direct-reply is not used new queue will be create in format '{queumame}.response.{UUID}'
        // Exchange = name of the exchange we want to bind our queue to
        return BindingBuilder
                .bind("rpc.request.log")
                .to(new DirectExchange("exchange.rpc.logs"))
                .build()
                ;
    }
}

