package examples.rpc;

import hoplin.io.Binding;
import examples.BaseExample;
import examples.LogDetail;
import hoplin.io.BindingBuilder;
import hoplin.io.FanoutExchange;
import hoplin.io.rpc.Rpc;
import hoplin.io.rpc.RpcServer;
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

        final RpcServer<LogDetail, String> server = Rpc.server(options(), binding);
        server.start(RpcServerExample::handler);

        Thread.currentThread().join();
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

