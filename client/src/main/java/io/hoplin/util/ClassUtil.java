package io.hoplin.util;

public class ClassUtil {

  /**
   * Get name of the package that the method was invoked from
   *
   * @return name of the package
   */
  public static String getRootPackageName() {
    final StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
    final StackTraceElement ste = stElements[stElements.length - 1];
    return ste.getClassName().substring(0, ste.getClassName().lastIndexOf("."));
  }

}
