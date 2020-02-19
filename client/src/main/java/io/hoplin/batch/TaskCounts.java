package io.hoplin.batch;

/**
 * Hold information regarding specific batch job
 */
public class TaskCounts {

    // Gets the number of tasks in the active state.
    private long active;

    // Gets the number of tasks in the completed state.
    private long completed;

    // Gets the number of tasks which failed
    private long failed;

    private long running;

    // Gets the number of tasks which succeeded.
    private long succeeded;
}
