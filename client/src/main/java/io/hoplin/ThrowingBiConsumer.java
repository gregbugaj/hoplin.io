package io.hoplin;

/**
 * Consumer capable of throwing exceptions
 *
 * @param <T>
 * @param <U>
 * @param <E>
 */
@FunctionalInterface
interface ThrowingBiConsumer<T, U, E extends Exception> {

  void accept(T t, U u) throws E;
}
