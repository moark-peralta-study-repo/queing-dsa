package org.hospitalqueing.service;

import java.util.List;

import org.hospitalqueing.dao.PatientDAO;
import org.hospitalqueing.model.Patient;

public class PatientService {
  PatientDAO patientDAO;

  public PatientService(PatientDAO patientDAO) {
    this.patientDAO = patientDAO;
  }

  public void createPatient(Patient patient) {
    patientDAO.save(patient);
  }

  public Patient getPatientById(int patientId) {
    return patientDAO.findById(patientId);
  }

  public List<Patient> getAllPatients() {
    return patientDAO.findAll();
  }

  public void updatePatient(Patient patient) {
    patientDAO.update(patient);
  }

  public void deletePatient(int patientId) {
    patientDAO.delete(patientId);
  }

  public Patient getPatientByUser(int userId) {
    return patientDAO.findByUserId(userId);
  }

  /** Soft-deletes a patient row (its account's patient profile lands in the trash bin). */
  public void softDeletePatient(int patientId) {
    patientDAO.softDelete(patientId);
  }

  /** Restores a trashed patient row back to live. */
  public void restorePatient(int patientId) {
    patientDAO.restore(patientId);
  }

  /** Permanently removes a trashed patient row. */
  public void permanentlyDeletePatient(int patientId) {
    patientDAO.permanentlyDelete(patientId);
  }
}
