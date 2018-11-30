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

    public <T> void enque(final Supplier<T> task)
    {
        Objects.requireNonNull(task);
        final T value = task.get();
        if(value == null)
            throw new NullPointerException("Task can't be null");
        tasks.add(value);
    }

    protected List<Object> getSubmittedTasks()
    {
        return tasks;
    }
}
