package org.hospitalqueing.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Appointment {
  private int appointmentId;
  private int patientId;
  private int serviceId;
  private Integer doctorId;
  private LocalDate appointmentDate;
  private LocalTime appointmentTime;
  private String status;
  private LocalDateTime createdAt;

  public Appointment() {}

  public Appointment(
      int appointmentId,
      int patientId,
      int serviceId,
      Integer doctorId,
      LocalDate appointmentDate,
      LocalTime appointmentTime,
      String status,
      LocalDateTime createdAt) {
    this.appointmentId = appointmentId;
    this.patientId = patientId;
    this.serviceId = serviceId;
    this.doctorId = doctorId;
    this.appointmentDate = appointmentDate;
    this.appointmentTime = appointmentTime;
    this.status = status;
    this.createdAt = createdAt;
  }

  public int getAppointmentId() {
    return appointmentId;
  }

  public void setAppointmentId(int appointmentId) {
    this.appointmentId = appointmentId;
  }

  public int getPatientId() {
    return patientId;
  }

  public void setPatientId(int patientId) {
    this.patientId = patientId;
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

  public LocalDate getAppointmentDate() {
    return appointmentDate;
  }

  public void setAppointmentDate(LocalDate appointmentDate) {
    this.appointmentDate = appointmentDate;
  }

  public LocalTime getAppointmentTime() {
    return appointmentTime;
  }

  public void setAppointmentTime(LocalTime appointmentTime) {
    this.appointmentTime = appointmentTime;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
