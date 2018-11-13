package io.hoplin;

@FunctionalInterface
interface ThrowingBiConsumer<T, U, E extends Exception>
{
     void accept(T t, U u) throws E;
}
