package io.hoplin;

import java.util.concurrent.TimeUnit;

/**
 * Information about the execution of an specific job
 */
public class JobExecutionInformation {
    private long startTime;

    private long endTime;

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long asElapsedNanon() {
        return endTime - startTime;
    }

    public long asElapsedMillis() {
        return TimeUnit.NANOSECONDS.toMillis(asElapsedNanon());
    }
}
