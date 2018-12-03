package io.hoplin.batch;

import java.util.Date;
import java.util.UUID;

public class BatchContextTask
{
    private Object message;

    private Date startDate;

    private boolean completed;

    private UUID taskId;

    public <T> BatchContextTask(final T taskMessage)
    {
        message = taskMessage;
    }

    public Object getMessage()
    {
        return message;
    }

    public BatchContextTask setMessage(final Object message)
    {
        this.message = message;
        return this;
    }

    public Date getStartDate()
    {
        return startDate;
    }

    public BatchContextTask setStartDate(final Date startDate)
    {
        this.startDate = startDate;
        return this;
    }

    public boolean isCompleted()
    {
        return completed;
    }

    public BatchContextTask setCompleted(final boolean completed)
    {
        this.completed = completed;
        return this;
    }

    public UUID getTaskId()
    {
        return taskId;
    }

    public BatchContextTask setTaskId(final UUID taskId)
    {
        this.taskId = taskId;
        return this;
    }
}
