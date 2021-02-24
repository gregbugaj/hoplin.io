package io.hoplin.benchmark.model;

public class TestCodecMappingTuple {

  private long valA;

  private long valB;

  public long getValA() {
    return valA;
  }

  public TestCodecMappingTuple setValA(long valA) {
    this.valA = valA;
    return this;
  }

  public long getValB() {
    return valB;
  }

  public TestCodecMappingTuple setValB(long valB) {
    this.valB = valB;
    return this;
  }

  @Override
  public String toString() {
    return "TestCodecMappingTuple{" +
        "valA=" + valA +
        ", valB=" + valB +
        '}';
  }
}
