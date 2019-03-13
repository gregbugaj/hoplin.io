package examples.rpc;

import examples.BaseExample;
import io.hoplin.Binding;
import io.hoplin.BindingBuilder;
import io.hoplin.DirectExchange;
import io.hoplin.rpc.DefaultRpcServer;
import io.hoplin.rpc.RpcServer;
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

        final RpcServer<LogDetailRequest, LogDetailResponse> server = DefaultRpcServer.create(options(), binding);
        server.respondAsync(RpcServerExample::handler);

        Thread.currentThread().join();
    }

    private static LogDetailResponse handler(final LogDetailRequest log)
    {
        if(true)
        {
             throw new RuntimeException("Faulty message handler");
        }

        return new LogDetailResponse("response", "info");
    }

    private static Binding bind()
    {
        return BindingBuilder
                .bind("rpc.request.log")
                .to(new DirectExchange("exchange.rpc.logs"))
                .build()
                ;
    }
}

