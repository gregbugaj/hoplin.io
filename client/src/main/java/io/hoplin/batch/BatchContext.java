package io.hoplin.batch;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Context in which the batch will execute
 */
public class BatchContext
{
    private List<Object> tasks = new ArrayList<>();

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

        tasks.add(taskMessage);
    }

    /**
     * Get list of sumbitted tasks
     * @return
     */
    protected List<Object> getSubmittedTasks()
    {
        return tasks;
    }
}
