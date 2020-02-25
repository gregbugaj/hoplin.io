package io.hoplin.model;

public class TestCodecMapping {

  private long valA;

  private long valB;

  private String msg;

  private TestCodecMappingVal mapping;

  public long getValA() {
    return valA;
  }

  public TestCodecMapping setValA(long valA) {
    this.valA = valA;
    return this;
  }

  public long getValB() {
    return valB;
  }

  public TestCodecMapping setValB(long valB) {
    this.valB = valB;
    return this;
  }

  public String getMsg() {
    return msg;
  }

  public TestCodecMapping setMsg(String msg) {
    this.msg = msg;
    return this;
  }

  public TestCodecMappingVal getMapping() {
    return mapping;
  }

  public TestCodecMapping setMapping(TestCodecMappingVal mapping) {
    this.mapping = mapping;
    return this;
  }

  @Override
  public String toString() {
    return "TestCodecMapping{" +
        "valA=" + valA +
        ", valB=" + valB +
        ", msg='" + msg + '\'' +
        ", mapping=" + mapping +
        '}';
  }
}
