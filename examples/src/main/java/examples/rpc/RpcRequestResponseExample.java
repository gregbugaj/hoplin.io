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
 * RPC Request/Response example
 */
public class RpcRequestResponseExample extends BaseExample
{
    private static final Logger log = LoggerFactory.getLogger(RpcRequestResponseExample.class);

    public static void main(final String... args) throws IOException, InterruptedException
    {
        final RpcClient<LogDetailRequest, LogDetailResponse> client = DefaultRpcClient.create(options(), bind());
        // rpc response
        client.respondAsync((request)->
        {
            final LogDetailResponse response = new LogDetailResponse("Response message", "info");
            return response;
        });

        // rpc request
        final LogDetailResponse response = client.request(new LogDetailRequest("Request message", "info"));
        log.info("RPC response : {} ", response);

        Thread.currentThread().join();
    }

    private static Binding bind()
    {
        return BindingBuilder
                .bind("rpc.direct.log")
                .to(new FanoutExchange("direct.rpc.logs"));
    }
}

