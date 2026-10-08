package org.hospitalqueing.service;

import java.util.List;

import org.hospitalqueing.dao.AppointmentDAO;
import org.hospitalqueing.model.Appointment;

public class AppointmentService {
  private final AppointmentDAO appointmentDAO;

  public AppointmentService(AppointmentDAO appointmentDAO) {
    this.appointmentDAO = appointmentDAO;
  }

  public void createAppointment(Appointment appointment) {
    appointmentDAO.save(appointment);
  }

  public Appointment getAppointmentById(int appointmentId) {
    return appointmentDAO.findById(appointmentId);
  }

  public List<Appointment> getAllAppointments() {
    return appointmentDAO.findAll();
  }

  public List<Appointment> getAppointmentsByPatient(int patientId) {
    return appointmentDAO.findByPatient(patientId);
  }

  public void updateAppointment(Appointment appointment) {
    appointmentDAO.update(appointment);
  }

  public void cancelAppointment(int appointmentId) {
    Appointment appointment = appointmentDAO.findById(appointmentId);

    if (appointment != null) {
      appointment.setStatus("CANCELLED");
      appointmentDAO.update(appointment);
    }
  }

  /**
   * Flips a SCHEDULED booking to CONFIRMED (the doctor dashboard's per-row Confirm action, T2).
   * No-op when the appointment does not exist or is not SCHEDULED, so re-confirming is safe.
   */
  public void confirmAppointment(int appointmentId) {
    Appointment appointment = appointmentDAO.findById(appointmentId);
    if (appointment != null && "SCHEDULED".equalsIgnoreCase(appointment.getStatus())) {
      appointment.setStatus("CONFIRMED");
      appointmentDAO.update(appointment);
    }
  }

  public void deleteAppointment(int appointmentId) {
    appointmentDAO.delete(appointmentId);
  }
}
