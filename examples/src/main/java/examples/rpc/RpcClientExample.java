package examples.rpc;

import examples.BaseExample;
import examples.LogDetail;
import io.hoplin.Binding;
import io.hoplin.BindingBuilder;
import io.hoplin.FanoutExchange;
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
        final LogDetailResponse response = client.request(new LogDetailRequest("Request message", "info"));

        log.info("RPC response : {} ", response);
    }

    private static void request(RpcClient<LogDetail, String> client)
    {
        long s = System.currentTimeMillis();
        int tickets  = 10;

        for(int i = 0; i < tickets; i++)
        {
            final String reply = client.request(new LogDetail("Msg : " + System.nanoTime(), "info"));
            System.out.println("Reply : " + reply);
        }

        long e = System.currentTimeMillis() - s;
        System.out.printf("time : " + e);
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
        return BindingBuilder
                .bind("rpc.request.log")
                .to(new FanoutExchange("rpc.logs"));
    }
}

