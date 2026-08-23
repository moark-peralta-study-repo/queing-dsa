package org.hospitalqueing.model;

import java.time.LocalDateTime;

public class QueueEvent {

  private int eventId;
  private int queueId;
  private Integer staffId;
  private String eventType;
  private String notes;
  private LocalDateTime createdAt;

  public QueueEvent() {}

  public QueueEvent(
      int eventId,
      int queueId,
      Integer staffId,
      String eventType,
      String notes,
      LocalDateTime createdAt) {

    this.eventId = eventId;
    this.queueId = queueId;
    this.staffId = staffId;
    this.eventType = eventType;
    this.notes = notes;
    this.createdAt = createdAt;
  }

  public int getEventId() {
    return eventId;
  }

  public void setEventId(int eventId) {
    this.eventId = eventId;
  }

  public int getQueueId() {
    return queueId;
  }

  public void setQueueId(int queueId) {
    this.queueId = queueId;
  }

  public Integer getStaffId() {
    return staffId;
  }

  public void setStaffId(Integer staffId) {
    this.staffId = staffId;
  }

  public String getEventType() {
    return eventType;
  }

  public void setEventType(String eventType) {
    this.eventType = eventType;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
