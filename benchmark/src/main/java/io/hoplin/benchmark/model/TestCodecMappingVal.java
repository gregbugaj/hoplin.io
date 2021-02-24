package io.hoplin.benchmark.model;


public class TestCodecMappingVal {

  private long valA;

  public long getValA() {
    return valA;
  }

  public TestCodecMappingVal setValA(long valA) {
    this.valA = valA;
    return this;
  }

  @Override
  public String toString() {
    return "TestCodecMappingVal{" +
        "valA=" + valA +
        '}';
  }
}
