package examples.rpc;

import examples.BaseExample;
import examples.LogDetail;
import hoplin.io.Binding;
import hoplin.io.BindingBuilder;
import hoplin.io.FanoutExchange;
import hoplin.io.rpc.Rpc;
import hoplin.io.rpc.RpcClient;
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

    public static void main(final String... args) throws InterruptedException, IOException
    {
        final Binding binding = bind();
        log.info("Binding : {}", binding);
        final RpcClient<LogDetail, String> client = Rpc.client(options(), binding);

        request(client);
//        asyncRequest(client);

        client.disconnect();
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
                .bind()
                .to(new FanoutExchange(""));
    }

}

