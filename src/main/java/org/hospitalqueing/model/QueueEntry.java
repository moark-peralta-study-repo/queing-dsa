package org.hospitalqueing.model;

public class QueueEntry {
  private int queueId;
  private int patientId;
  private int departmentId;
  private int serviceId;
  private Integer doctorId;
  private Integer appointmentId;
  private Integer counterId;

  private String queueDate;
  private int queueNumber;
  private String priorityType;
  private String status;
  private String qrToken;

  private String joinedAt;
  private String calledAt;
  private String serviceStartedAt;
  private String completedAt;

  public QueueEntry() {}

  public QueueEntry(
      int queueId,
      int patientId,
      int departmentId,
      int serviceId,
      Integer doctorId,
      Integer appointmentId,
      Integer counterId,
      String queueDate,
      int queueNumber,
      String priorityType,
      String status,
      String qrToken,
      String joinedAt,
      String calledAt,
      String serviceStartedAt,
      String completedAt) {
    this.queueId = queueId;
    this.patientId = patientId;
    this.departmentId = departmentId;
    this.serviceId = serviceId;
    this.doctorId = doctorId;
    this.appointmentId = appointmentId;
    this.counterId = counterId;
    this.queueDate = queueDate;
    this.queueNumber = queueNumber;
    this.priorityType = priorityType;
    this.status = status;
    this.qrToken = qrToken;
    this.joinedAt = joinedAt;
    this.calledAt = calledAt;
    this.serviceStartedAt = serviceStartedAt;
    this.completedAt = completedAt;
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

  public int getDepartmentId() {
    return departmentId;
  }

  public void setDepartmentId(int departmentId) {
    this.departmentId = departmentId;
  }

  public int getServiceId() {
    return serviceId;
  }

  public void setServiceId(int serviceId) {
    this.serviceId = serviceId;
  }

  public Integer getDoctorId() {
    return doctorId;
  }

  public void setDoctorId(Integer doctorId) {
    this.doctorId = doctorId;
  }

  public Integer getAppointmentId() {
    return appointmentId;
  }

  public void setAppointmentId(Integer appointmentId) {
    this.appointmentId = appointmentId;
  }

  public Integer getCounterId() {
    return counterId;
  }

  public void setCounterId(Integer counterId) {
    this.counterId = counterId;
  }

  public String getQueueDate() {
    return queueDate;
  }

  public void setQueueDate(String queueDate) {
    this.queueDate = queueDate;
  }

  public int getQueueNumber() {
    return queueNumber;
  }

  public void setQueueNumber(int queueNumber) {
    this.queueNumber = queueNumber;
  }

  public String getPriorityType() {
    return priorityType;
  }

  public void setPriorityType(String priorityType) {
    this.priorityType = priorityType;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getQrToken() {
    return qrToken;
  }

  public void setQrToken(String qrToken) {
    this.qrToken = qrToken;
  }

  public String getJoinedAt() {
    return joinedAt;
  }

  public void setJoinedAt(String joinedAt) {
    this.joinedAt = joinedAt;
  }

  public String getCalledAt() {
    return calledAt;
  }

  public void setCalledAt(String calledAt) {
    this.calledAt = calledAt;
  }

  public String getServiceStartedAt() {
    return serviceStartedAt;
  }

  public void setServiceStartedAt(String serviceStartedAt) {
    this.serviceStartedAt = serviceStartedAt;
  }

  public String getCompletedAt() {
    return completedAt;
  }

  public void setCompletedAt(String completedAt) {
    this.completedAt = completedAt;
  }
}
