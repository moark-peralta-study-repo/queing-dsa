package org.hospitalqueing.model;

import java.time.LocalTime;

public class Notification {
  private int notificationId;
  private int patientId;
  private String channel;
  private String message;

  private String status;
  private LocalTime sentAt;

  public Notification() {}

  public Notification(
      int notificationId,
      int patientId,
      String channel,
      String message,
      String status,
      LocalTime sentAt) {
    this.notificationId = notificationId;
    this.patientId = patientId;
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

  public LocalTime getSentAt() {
    return sentAt;
  }

  public void setSentAt(LocalTime sentAt) {
    this.sentAt = sentAt;
  }
}
