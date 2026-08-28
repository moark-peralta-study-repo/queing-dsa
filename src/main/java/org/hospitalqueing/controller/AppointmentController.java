package org.hospitalqueing.controller;

import java.util.List;

import org.hospitalqueing.model.Appointment;
import org.hospitalqueing.service.AppointmentService;

public class AppointmentController {
  private final AppointmentService appointmentService;

  public AppointmentController(AppointmentService appointmentService) {
    this.appointmentService = appointmentService;
  }

  public void createAppointment(Appointment appointment) {
    appointmentService.createAppointment(appointment);
  }

  public Appointment getAppointment(int appointmentId) {
    return appointmentService.getAppointmentById(appointmentId);
  }

  public List<Appointment> getAllAppointments() {
    return appointmentService.getAllAppointments();
  }

  public List<Appointment> getAppointmentsByPatient(int patientId) {
    return appointmentService.getAppointmentsByPatient(patientId);
  }

  public void updateAppointment(Appointment appointment) {
    appointmentService.updateAppointment(appointment);
  }

  public void cancelAppointment(int appointmentId) {
    appointmentService.cancelAppointment(appointmentId);
  }

  public void deleteAppointment(int appointmentId) {
    appointmentService.deleteAppointment(appointmentId);
  }
}
