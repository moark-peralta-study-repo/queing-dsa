package org.hospitalqueing.model;

import java.time.LocalDateTime;

public class Payment {
  private int paymentId;
  private int queueId;
  private double amount;
  private String paymentMethod;
  private String status;
  private LocalDateTime paidAt;

  public Payment() {}

  public Payment(
      int paymentId,
      int queueId,
      double amount,
      String paymentMethod,
      String status,
      LocalDateTime paidAt) {
    this.paymentId = paymentId;
    this.queueId = queueId;
    this.amount = amount;
    this.paymentMethod = paymentMethod;
    this.status = status;
    this.paidAt = paidAt;
  }

  public int getPaymentId() {
    return paymentId;
  }

  public void setPaymentId(int paymentId) {
    this.paymentId = paymentId;
  }

  public int getQueueId() {
    return queueId;
  }

  public void setQueueId(int queueId) {
    this.queueId = queueId;
  }

  public double getAmount() {
    return amount;
  }

  public void setAmount(double amount) {
    this.amount = amount;
  }

  public String getPaymentMethod() {
    return paymentMethod;
  }

  public void setPaymentMethod(String paymentMethod) {
    this.paymentMethod = paymentMethod;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public LocalDateTime getPaidAt() {
    return paidAt;
  }

  public void setPaidAt(LocalDateTime paidAt) {
    this.paidAt = paidAt;
  }
}
