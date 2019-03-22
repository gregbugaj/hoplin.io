package io.hoplin.batch;

import java.time.Duration;
import java.util.*;
import java.util.function.Supplier;

/**
 * Context in which the batch will execute
 */
public class BatchContext implements Iterable<BatchContextTask>
{
    private final UUID batchId = UUID.randomUUID();

    private List<BatchContextTask> tasks = new ArrayList<>();

    /** Time the batch was started */
    private Date startedAt = new Date();

    /** Time the batch was completed */
    private Date completedAt;

    /** Indicates that the batch have been completed */
    private boolean completed;

    private long taskCounter;

    /**
     * Enque new batch request
     *
     * @param taskSupplier
     * @param <T>
     */
    public <T> void enqueue(final Supplier<T> taskSupplier)
    {
        Objects.requireNonNull(taskSupplier);
        final T taskMessage = taskSupplier.get();

        if(taskMessage == null)
            throw new NullPointerException("Task message can't be null");

        taskCounter++;
        tasks.add(new BatchContextTask(taskMessage));
    }

    long decrementAndGetTaskCount()
    {
        final long count = --taskCounter;
        if(count == 0)
        {
            completed = true;
            completedAt  = new Date();
        }
        return count;
    }

    /**
     * Time it took to process the message
     * @return
     */
    public Duration duration()
    {
        return Duration.between(startedAt.toInstant(), completedAt.toInstant());
    }

    /**
     * Get list of submitted tasks
     * @return
     */
    protected List<BatchContextTask> getSubmittedTasks()
    {
        return tasks;
    }

    /**
     * Id that identifies this batch
     * @return
     */
    public UUID getBatchId()
    {
        return batchId;
    }

    @Override
    public Iterator<BatchContextTask> iterator()
    {
        return tasks.iterator();
    }

    public boolean isCompleted()
    {
        return completed;
    }

    public Date getStartedAt()
    {
        return startedAt;
    }

    public BatchContext setStartedAt(final Date startedAt)
    {
        this.startedAt = startedAt;
        return this;
    }

    public Date getCompletedAt()
    {
        return completedAt;
    }

    public BatchContext setCompletedAt(final Date completedAt)
    {
        this.completedAt = completedAt;
        return this;
    }
}
