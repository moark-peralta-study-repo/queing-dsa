package org.hospitalqueing.controller;

import java.util.List;

import org.hospitalqueing.model.Doctor;
import org.hospitalqueing.service.DoctorService;

public class DoctorController {
  private final DoctorService doctorService;

  public DoctorController(DoctorService doctorService) {
    this.doctorService = doctorService;
  }

  public void createDoctor(Doctor doctor) {
    doctorService.createDoctor(doctor);
  }

  public Doctor getDoctor(int doctorId) {
    return doctorService.getDoctorById(doctorId);
  }

  public List<Doctor> getAllDoctors() {
    return doctorService.getAllDoctors();
  }

  public List<Doctor> getDoctorsByDepartment(int departmentId) {
    return doctorService.getDoctorsByDepartment(departmentId);
  }

  public void updateDoctor(Doctor doctor) {
    doctorService.updateDoctor(doctor);
  }

  public void deleteDoctor(int doctorId) {
    doctorService.deleteDoctor(doctorId);
  }
}
