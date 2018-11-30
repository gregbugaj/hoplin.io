package io.hoplin.rpc;

/**
 * RpClientServer
 *
 * @param <I>
 * @param <O>
 */
public interface RpClientServer<I, O> extends RpcClient<I, O>, RpcServer<I, O>
{
}
