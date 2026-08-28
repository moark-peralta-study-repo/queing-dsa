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
}
