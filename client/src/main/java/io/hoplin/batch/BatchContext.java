package io.hoplin.batch;

import java.util.*;
import java.util.function.Supplier;

/**
 * Context in which the batch will execute
 */
public class BatchContext
{
    private final UUID batchId = UUID.randomUUID();

    private List<BatchContextTask> tasks = new ArrayList<>();

    /** Time the batch was started */
    private Date startedAt;

    /** Time the batch was completed */
    private Date completedAt;

    /**
     * Enque new batch request
     *
     * @param taskSupplier
     * @param <T>
     */
    public <T> void enque(final Supplier<T> taskSupplier)
    {
        Objects.requireNonNull(taskSupplier);
        final T taskMessage = taskSupplier.get();

        if(taskMessage == null)
            throw new NullPointerException("Task message can't be null");

        tasks.add(new BatchContextTask(taskMessage));
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
}
