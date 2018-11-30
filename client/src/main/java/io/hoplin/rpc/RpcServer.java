package io.hoplin.rpc;

import java.util.function.Function;

public interface RpcServer<I, O>
{
    void respondAsync(final Function<I, O> handler);
}
