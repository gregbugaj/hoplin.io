package examples.rpc;

import examples.BaseExample;
import io.hoplin.Binding;
import io.hoplin.BindingBuilder;
import io.hoplin.FanoutExchange;
import io.hoplin.rpc.DefaultRpcClient;
import io.hoplin.rpc.RpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RPC Server example
 */
public class RpcServerExample extends BaseExample
{
    private static final Logger log = LoggerFactory.getLogger(RpcServerExample.class);

    public static void main(final String... args) throws InterruptedException
    {
        final Binding binding = bind();
        log.info("Binding : {}", binding);

        final RpcClient<LogDetailRequest, LogDetailResponse> client = DefaultRpcClient.create(options(), binding);
        client.respondAsync(RpcServerExample::handler);

        Thread.currentThread().join();
    }

    private static LogDetailResponse handler(final LogDetailRequest log)
    {
        return new LogDetailResponse("response", "info");
    }

    private static Binding bind()
    {
        return BindingBuilder
                .bind("rpc.request.log")
                .to(new FanoutExchange("rpc.logs"));
    }
}

