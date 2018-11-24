package io.hoplin.util;

public class ClassUtil
{
    /**
     * Get name of the main class
     * @return
     */
    public static String getMainClass()
    {
        final StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
        final StackTraceElement ste = stElements[stElements.length - 1];
        return ste.getClassName();
    }
}
