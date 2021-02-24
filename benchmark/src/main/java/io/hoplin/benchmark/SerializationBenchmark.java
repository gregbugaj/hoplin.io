package io.hoplin.benchmark;


import io.hoplin.MessagePayload;
import io.hoplin.json.JsonMessagePayloadCodec;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class SerializationBenchmark {

  @State(Scope.Benchmark)
  public static class Context {

    public long id;

    @Setup
    public void setup() {

    }

    @TearDown(Level.Iteration)
    public void teardown() {
      id = 0;
    }

    @Benchmark
    @Fork(value = 1)
    @Measurement(iterations = 3, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
    @Warmup(iterations = 1, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
    public void benchmark(final Context context) {
      final JsonMessagePayloadCodec codec = new JsonMessagePayloadCodec();
      final Long[] val = new Long[]{10L, 20L, 30L};
      final byte[] data = codec.serialize(val);
      final MessagePayload<?> out = codec.deserialize(data, MessagePayload.class);

      context.id++;
    }
  }
}
