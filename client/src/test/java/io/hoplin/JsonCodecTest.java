package io.hoplin;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.hoplin.json.JsonMessagePayloadCodec;
import io.hoplin.model.TestCodecMapping;
import io.hoplin.model.TestCodecMappingTuple;
import io.hoplin.model.TestCodecMappingVal;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class JsonCodecTest {

  @Test
  public void codecMessageString001() {
    final JsonMessagePayloadCodec codec = new JsonMessagePayloadCodec();
    final String val = "MessageToProcess";
    final byte[] data = codec.serialize(val);
    final MessagePayload out = codec.deserialize(data, MessagePayload.class);
    assertNotNull(out);
    assertEquals(String.class, out.getTypeAsClass());
    assertEquals(val, out.getPayload());
  }

  @Test
  public void codecMessageWrappedLong001() {
    final JsonMessagePayloadCodec codec = new JsonMessagePayloadCodec();
    final Long val = 100L;
    final byte[] data = codec.serialize(val);
    final MessagePayload out = codec.deserialize(data, MessagePayload.class);

    assertNotNull(out);
    assertEquals(Long.class, out.getTypeAsClass());
    assertEquals(val, out.getPayload());
  }

  @Test
  public void codecMessageWrappedDouble001() {
    final JsonMessagePayloadCodec codec = new JsonMessagePayloadCodec();
    final Double val = 100.51;
    final byte[] data = codec.serialize(val);
    final MessagePayload out = codec.deserialize(data, MessagePayload.class);

    assertNotNull(out);
    assertEquals(Double.class, out.getTypeAsClass());
    assertEquals(val, out.getPayload());
  }

  @Test
  public void codecMessageWrappedLongArray001() {
    final JsonMessagePayloadCodec codec = new JsonMessagePayloadCodec();
    final Long[] val = new Long[]{10L, 20L, 30L};
    final byte[] data = codec.serialize(val);
    final MessagePayload out = codec.deserialize(data, MessagePayload.class);

    assertNotNull(out);
    assertEquals(Long[].class, out.getTypeAsClass());
    assertArrayEquals(val, (Long[]) out.getPayload());
  }

  @Test
  public void codecMessageWrappedStringArray001() {
    final JsonMessagePayloadCodec codec = new JsonMessagePayloadCodec();
    final String[] val = new String[]{"Msg1", "Msg2", "Msg3"};
    final byte[] data = codec.serialize(val);

    final MessagePayload out = codec.deserialize(data, MessagePayload.class);

    assertNotNull(out);
    assertEquals(String[].class, out.getTypeAsClass());
    assertArrayEquals(val, (String[]) out.getPayload());
  }

  @Test
  public void codecReflectionClassDeterminationSimpleObject() {

    final JsonMessagePayloadCodec codec = new JsonMessagePayloadCodec(getClasses());
    final TestCodecMapping val = new TestCodecMapping();
    val.setMsg("Msg A");
    val.setValA(1);
    val.setValB(2);

    final byte[] data = codec.serialize(val);
    final MessagePayload out = codec.deserialize(data, MessagePayload.class);
    assertNotNull(out);
    assertEquals(TestCodecMapping.class, out.getTypeAsClass());
  }

  @Test
  public void codecReflectionClassDeterminationComposite() {

    final JsonMessagePayloadCodec codec = new JsonMessagePayloadCodec(getClasses());

    final TestCodecMapping val = new TestCodecMapping();
    val.setMsg("Msg A");
    val.setValA(1);
    val.setValB(2);

    final TestCodecMappingVal mapping = new TestCodecMappingVal();
    mapping.setValA(111);

    val.setMapping(mapping);

    final MessagePayload pay = new MessagePayload();
    pay.setPayload(mapping);
    pay.setType(mapping.getClass());

    final byte[] dataxx = codec.serialize(pay);
    System.out.println(new String(dataxx));

    final byte[] data = codec.serialize(val);
    final MessagePayload out = codec.deserialize(data, MessagePayload.class);

    assertNotNull(out);
    assertEquals(TestCodecMapping.class, out.getTypeAsClass());
  }


  @Test
  public void codecReflectionClassDeterminationDisambiguity001() {
    final JsonMessagePayloadCodec codec = new JsonMessagePayloadCodec(getClasses());

    final TestCodecMapping val = new TestCodecMapping();
    val.setMsg("Msg A");
    val.setValA(1);
    val.setValB(2);

    final TestCodecMappingVal mapping = new TestCodecMappingVal();
    mapping.setValA(111);
    val.setMapping(mapping);

    final byte[] data = codec.serialize(mapping);
    final MessagePayload out = codec.deserialize(data, MessagePayload.class);

    assertNotNull(out);
    assertEquals(TestCodecMappingVal.class, out.getTypeAsClass());
  }


  @Test
  public void codecReflectionClassDeterminationDisambiguity002() {

    final Set<Class<?>> mappings = getClasses();
    final JsonMessagePayloadCodec codec = new JsonMessagePayloadCodec(mappings);

    final TestCodecMapping val = new TestCodecMapping();
    val.setMsg("Msg A");
    val.setValA(1);
    val.setValB(2);

    TestCodecMappingTuple tuple = new TestCodecMappingTuple();
    final byte[] data = codec.serialize(tuple);
    final MessagePayload out = codec.deserialize(data, MessagePayload.class);

    assertNotNull(out);
    assertEquals(TestCodecMappingTuple.class, out.getTypeAsClass());
  }

  private Set<Class<?>> getClasses() {
    final Set<Class<?>> mappings = new HashSet<>();
    mappings.add(TestCodecMapping.class);
    mappings.add(TestCodecMappingTuple.class);
    mappings.add(TestCodecMappingVal.class);
    return mappings;
  }
}
