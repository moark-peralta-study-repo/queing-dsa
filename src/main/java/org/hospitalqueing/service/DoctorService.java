package org.hospitalqueing.service;

import java.util.List;

import org.hospitalqueing.dao.DoctorDAO;
import org.hospitalqueing.model.Doctor;

public class DoctorService {
  private final DoctorDAO doctorDAO;

  public DoctorService(DoctorDAO doctorDAO) {
    this.doctorDAO = doctorDAO;
  }

  public void createDoctor(Doctor doctor) {
    doctorDAO.save(doctor);
  }

  public Doctor getDoctorById(int doctorId) {
    return doctorDAO.findById(doctorId);
  }

  public List<Doctor> getAllDoctors() {
    return doctorDAO.findAll();
  }

  public List<Doctor> getDoctorsByDepartment(int departmentId) {
    return doctorDAO.findByDepartment(departmentId);
  }

  public void updateDoctor(Doctor doctor) {
    doctorDAO.update(doctor);
  }

  public void deleteDoctor(int doctorId) {
    doctorDAO.delete(doctorId);
  }
}
