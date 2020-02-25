package io.hoplin.batch;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Context in which the batch will execute
 * Batch = Job
 */
public class BatchContext implements Iterable<BatchContextTask> {

  /**
   * Unique Batch Id
   */
  private final UUID batchId = UUID.randomUUID();

  /**
   * Task associated with this batch
   */
  private final List<BatchContextTask> tasks = new ArrayList<>();

  /**
   * Time the batch was started
   */
  private Date startedAt = new Date();

  /**
   * Time the batch was completed
   */
  private Date completedAt;

  /**
   * Indicates that the batch have been completed
   */
  private boolean completed;

  private long taskCounter;

  /**
   * Job priority
   * Priority values can range from -1000 to 1000, with -1000 being the lowest priority and 1000 being the highest priority
   */
  private int priority;

  /**
   * Enqueue new batch request
   *
   * @param taskSupplier
   * @param <T>
   */
  public <T> void enqueue(final Supplier<T> taskSupplier) {
    Objects.requireNonNull(taskSupplier, "Supplier can't be null");
    final T taskMessage = taskSupplier.get();

      if (taskMessage == null) {
          throw new NullPointerException("Task message can't be null");
      }

    taskCounter++;
    tasks.add(new BatchContextTask(taskMessage));
  }

  long decrementAndGetTaskCount() {
    final long count = --taskCounter;
    if (count == 0) {
      completed = true;
      completedAt = new Date();
    }
    return count;
  }

  /**
   * Time it took to process this batch message
   *
   * @return time between start and complete time, if ether is null return {@link Duration#ZERO}
   */
  public Duration duration() {
    if (startedAt == null || completedAt == null) {
      return Duration.ZERO;
    }
    return Duration.between(startedAt.toInstant(), completedAt.toInstant());
  }

  /**
   * Get list of submitted tasks
   *
   * @return
   */
  protected List<BatchContextTask> getSubmittedTasks() {
    return tasks;
  }

  /**
   * Id that identifies this batch
   *
   * @return
   */
  public UUID getBatchId() {
    return batchId;
  }

  @Override
  public Iterator<BatchContextTask> iterator() {
    return tasks.iterator();
  }

  /**
   * Check if the batch is complete
   *
   * @return
   */
  public boolean isCompleted() {
    return completed;
  }

  /**
   * Get the time the batch have been started at
   *
   * @return
   */
  public Date getStartedAt() {
    return startedAt;
  }

  public BatchContext setStartedAt(final Date startedAt) {
    Objects.requireNonNull(startedAt);
    this.startedAt = new Date(startedAt.getTime());
    return this;
  }

  /**
   * Get time the batch has completed
   *
   * @return
   */
  public Date getCompletedAt() {
    return completedAt;
  }

  /**
   * Set batch completion time
   *
   * @param completedAt
   * @return
   */
  public BatchContext setCompletedAt(final Date completedAt) {
    Objects.requireNonNull(completedAt);
    this.completedAt = new Date(completedAt.getTime());
    return this;
  }
}
