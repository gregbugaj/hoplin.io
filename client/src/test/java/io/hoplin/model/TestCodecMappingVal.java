package io.hoplin.model;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class TestCodecMappingVal {

  private long valA;


  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.DEFAULT_STYLE);
  }

  public long getValA() {
    return valA;
  }

  public TestCodecMappingVal setValA(long valA) {
    this.valA = valA;
    return this;
  }
}
