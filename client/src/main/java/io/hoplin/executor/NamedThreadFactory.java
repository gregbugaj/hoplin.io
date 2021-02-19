package io.hoplin.executor;


import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Named thread factory for creating named threads
 */
public class NamedThreadFactory implements ThreadFactory {

  private static final Logger log = LoggerFactory.getLogger(NamedThreadFactory.class);

  private final AtomicLong id = new AtomicLong(0);

  private final ThreadGroup group;

  private final boolean daemon;

  private final String prefix;

  public NamedThreadFactory(final String name, final boolean daemon) {
    this.daemon = daemon;
    final SecurityManager sm = System.getSecurityManager();
    this.group = (sm != null) ? sm.getThreadGroup() : Thread.currentThread().getThreadGroup();
    this.prefix = name;
  }

  @Override
  public Thread newThread(final Runnable runnable) {
    final Thread thread = new Thread(group, runnable, prefix + "-" + id.incrementAndGet(), 0);
    thread.setDaemon(daemon);
    thread.setUncaughtExceptionHandler((th, e) -> log.error("Unable to execute thread : " + th, e));

    if (thread.getPriority() != Thread.NORM_PRIORITY) {
      thread.setPriority(Thread.NORM_PRIORITY);
    }

    return thread;
  }
}
