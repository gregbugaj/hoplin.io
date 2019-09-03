package examples;

public class LogDetail {

  private String msg;

  private String level;

  public LogDetail(final String msg, final String level) {
    this.msg = msg;
    this.level = level;
  }

  public String getMsg() {
    return msg;
  }

  public LogDetail setMsg(final String msg) {
    this.msg = msg;
    return this;
  }

  public String getLevel() {
    return level;
  }

  public LogDetail setLevel(final String level) {
    this.level = level;
    return this;
  }

  @Override
  public String toString() {
    return level + " : " + msg;
  }
}
