package org.hospitalqueing.service;

import java.util.List;

import org.hospitalqueing.dao.DoctorServiceDAO;
import org.hospitalqueing.model.DoctorService;

public class DoctorAssignmentService {
  private final DoctorServiceDAO doctorServiceDAO;

  DoctorAssignmentService(DoctorServiceDAO doctorServiceDAO) {
    this.doctorServiceDAO = doctorServiceDAO;
  }

  public void assignDoctorToService(int doctorId, int serviceId) {
    doctorServiceDAO.save(new DoctorService(doctorId, serviceId));
  }

  public List<DoctorService> getServicesByDoctor(int doctorId) {
    return doctorServiceDAO.findByDoctor(doctorId);
  }

  public List<DoctorService> getDoctorsByService(int serviceId) {
    return doctorServiceDAO.findByService(serviceId);
  }

  public void unassignDoctorFromService(int doctorId, int serviceId) {
    doctorServiceDAO.delete(doctorId, serviceId);
  }
}
