package org.hospitalqueing.model;

import java.time.LocalDateTime;

public class Notification {
  private int notificationId;
  private int patientId;
  private Integer queueId;
  private String channel;
  private String message;
  private String status;
  private LocalDateTime sentAt;

  public Notification() {}

  public Notification(
      int notificationId,
      int patientId,
      Integer queueId,
      String channel,
      String message,
      String status,
      LocalDateTime sentAt) {
    this.notificationId = notificationId;
    this.patientId = patientId;
    this.queueId = queueId;
    this.channel = channel;
    this.message = message;
    this.status = status;
    this.sentAt = sentAt;
  }

  public int getNotificationId() {
    return notificationId;
  }

  public void setNotificationId(int notificationId) {
    this.notificationId = notificationId;
  }

  public int getPatientId() {
    return patientId;
  }

  public void setPatientId(int patientId) {
    this.patientId = patientId;
  }

  public Integer getQueueId() {
    return queueId;
  }

  public void setQueueId(Integer queueId) {
    this.queueId = queueId;
  }

  public String getChannel() {
    return channel;
  }

  public void setChannel(String channel) {
    this.channel = channel;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public LocalDateTime getSentAt() {
    return sentAt;
  }

  public void setSentAt(LocalDateTime sentAt) {
    this.sentAt = sentAt;
  }
}
