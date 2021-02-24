package io.hoplin.benchmark;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * Benchmarking entrypoint
 */
public class Benchmark {

  public static void main(String[] args) throws RunnerException {

    final Options opt = new OptionsBuilder()
        .include(SerializationBenchmark.class.getSimpleName())
        .build();
    new Runner(opt).run();
  }
}
