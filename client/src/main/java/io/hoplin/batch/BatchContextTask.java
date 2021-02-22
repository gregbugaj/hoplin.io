package io.hoplin.batch;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

/**
 * Individual task detail for the job
 */
public class BatchContextTask {

  private Object message;

  private Date startDate;

  private boolean completed;

  private final UUID taskId;

  // reply value for the job
  private byte[] reply;

  public <T> BatchContextTask(final T message) {
    this.message = message;
    this.taskId = UUID.randomUUID();
  }

  public Object getMessage() {
    return message;
  }

  public BatchContextTask setMessage(final Object message) {
    this.message = message;
    return this;
  }

  public Date getStartDate() {
    return startDate;
  }

  public BatchContextTask setStartDate(final Date startDate) {
    this.startDate = startDate;
    return this;
  }

  public boolean isCompleted() {
    return completed;
  }

  public BatchContextTask setCompleted(final boolean completed) {
    this.completed = completed;
    return this;
  }

  public UUID getTaskId() {
    return taskId;
  }

  public void setReply(byte[] body) {
    this.reply = body;
  }

  public byte[] getReply() {
    return Arrays.copyOf(reply, reply.length);
  }

  public String getReplyAsString() {
    return new String(reply, StandardCharsets.UTF_8);
  }

  @Override
  public String toString() {
    return "BatchContextTask{" +
        "startDate=" + startDate +
        ", completed=" + completed +
        ", taskId=" + taskId +
        '}';
  }
}
