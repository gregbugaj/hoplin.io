package io.hoplin.benchmark;

import io.hoplin.MessagePayload;
import io.hoplin.benchmark.model.TestCodecMapping;
import io.hoplin.benchmark.model.TestCodecMappingTuple;
import io.hoplin.benchmark.model.TestCodecMappingVal;
import io.hoplin.json.JsonMessagePayloadCodec;
import java.util.HashSet;
import java.util.Set;
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

    public JsonMessagePayloadCodec codec;

    private Set<Class<?>> getClasses() {
      final Set<Class<?>> mappings = new HashSet<>();
      mappings.add(TestCodecMapping.class);
      mappings.add(TestCodecMappingTuple.class);
      mappings.add(TestCodecMappingVal.class);
      return mappings;
    }

    @Setup
    public void setup() {
      codec = new JsonMessagePayloadCodec(getClasses());
    }

    @TearDown(Level.Iteration)
    public void teardown() {
      id = 0;
    }

    @Benchmark
    @Fork(value = 1)
    @Measurement(iterations = 10, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
    @Warmup(iterations = 3, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
    public void benchmark_BasicObject(final Context context) {
      final Long[] val = new Long[]{10L, 20L, 30L};
      final byte[] data = codec.serialize(val);
      final MessagePayload<?> out = codec.deserialize(data, MessagePayload.class);

      context.id++;
    }

    @Benchmark
    @Fork(value = 1)
    @Measurement(iterations = 10, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
    @Warmup(iterations = 3, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
    public void benchmark_ComplexObject_1(final Context context) {

      final TestCodecMapping val = new TestCodecMapping();
      val.setMsg("Msg A");
      val.setValA(1);
      val.setValB(2);

      final TestCodecMappingVal mapping = new TestCodecMappingVal();
      mapping.setValA(111);

      val.setMapping(mapping);

      final byte[] data = codec.serialize(val);
      final MessagePayload<?> out = codec.deserialize(data, MessagePayload.class);

      context.id++;
    }


    @Benchmark
    @Fork(value = 1)
    @Measurement(iterations = 10, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
    @Warmup(iterations = 3, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
    public void benchmark_ComplexObject_2(final Context context) {
      TestCodecMappingTuple tuple = new TestCodecMappingTuple();
      final byte[] data = codec.serialize(tuple);
      final MessagePayload<?> out = codec.deserialize(data, MessagePayload.class);

      context.id++;
    }
  }
}
