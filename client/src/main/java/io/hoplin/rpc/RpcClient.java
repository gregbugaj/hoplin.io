package io.hoplin.rpc;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public interface RpcClient<I, O>
{
    O request(final I request);

    CompletableFuture<O> requestAsync(final I request);

    void respondAsync(final Function<I, O> handler);
}
