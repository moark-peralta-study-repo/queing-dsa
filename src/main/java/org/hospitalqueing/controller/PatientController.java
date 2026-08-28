package org.hospitalqueing.controller;

import java.util.List;

import org.hospitalqueing.model.Patient;
import org.hospitalqueing.service.PatientService;

public class PatientController {
  private final PatientService patientService;

  public PatientController(PatientService patientService) {
    this.patientService = patientService;
  }

  public void registerPatient(Patient patient) {
    patientService.createPatient(patient);
  }

  public Patient getPatient(int patientId) {
    return patientService.getPatientById(patientId);
  }

  public List<Patient> getAllPatients() {
    return patientService.getAllPatients();
  }

  public void updatePatient(Patient patient) {
    patientService.updatePatient(patient);
  }

  public void deletePatient(int patientId) {
    patientService.deletePatient(patientId);
  }
}
