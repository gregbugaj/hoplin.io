package examples.rpc;

import examples.BaseExample;
import examples.LogDetail;
import io.hoplin.Binding;
import io.hoplin.BindingBuilder;
import io.hoplin.FanoutExchange;
import io.hoplin.rpc.Rpc;
import io.hoplin.rpc.RpcServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * RPC Server example
 */
public class RpcServerExample extends BaseExample
{
    private static final Logger log = LoggerFactory.getLogger(RpcServerExample.class);

    public static void main(final String... args) {
        final Binding binding = bind();
        log.info("Binding : {}", binding);

        final RpcServer<LogDetail, String> server = Rpc.server(options(), binding);
        server.start(RpcServerExample::handler);

        //Thread.currentThread().join();
    }

    private static String handler(final LogDetail log)
    {
        return "ABC :"+ log;
    }

    private static Binding bind()
    {
        return BindingBuilder
                .bind()
                .to(new FanoutExchange(""));
    }
}

