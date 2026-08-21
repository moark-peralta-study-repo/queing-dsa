package org.hospitalqueing.model;

import java.time.LocalDateTime;

public class Feedback {
  private int feedbackId;
  private int queueId;
  private int patientId;
  private int rating;
  private String comment;
  private LocalDateTime createdAt;

  public Feedback() {}

  public Feedback(
      int feedbackId,
      int queueId,
      int patientId,
      int rating,
      String comment,
      LocalDateTime createdAt) {
    this.feedbackId = feedbackId;
    this.queueId = queueId;
    this.patientId = patientId;
    this.rating = rating;
    this.comment = comment;
    this.createdAt = createdAt;
  }

  public int getFeedbackId() {
    return feedbackId;
  }

  public void setFeedbackId(int feedbackId) {
    this.feedbackId = feedbackId;
  }

  public int getQueueId() {
    return queueId;
  }

  public void setQueueId(int queueId) {
    this.queueId = queueId;
  }

  public int getPatientId() {
    return patientId;
  }

  public void setPatientId(int patientId) {
    this.patientId = patientId;
  }

  public int getRating() {
    return rating;
  }

  public void setRating(int rating) {
    this.rating = rating;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
