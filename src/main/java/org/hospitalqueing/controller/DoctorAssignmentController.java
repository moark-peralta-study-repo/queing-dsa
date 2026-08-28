package org.hospitalqueing.controller;

import java.util.List;

import org.hospitalqueing.model.DoctorService;
import org.hospitalqueing.service.DoctorAssignmentService;

public class DoctorAssignmentController {
  private final DoctorAssignmentService doctorAssignmentService;

  public DoctorAssignmentController(DoctorAssignmentService doctorAssignmentService) {
    this.doctorAssignmentService = doctorAssignmentService;
  }

  public void assignDoctorToService(int doctorId, int serviceId) {
    doctorAssignmentService.assignDoctorToService(doctorId, serviceId);
  }

  public List<DoctorService> getServicesByDoctor(int doctorId) {
    return doctorAssignmentService.getServicesByDoctor(doctorId);
  }

  public List<DoctorService> getDoctorsByService(int serviceId) {
    return doctorAssignmentService.getDoctorsByService(serviceId);
  }

  public void unassignDoctorFromService(int doctorId, int serviceId) {
    doctorAssignmentService.unassignDoctorFromService(doctorId, serviceId);
  }
}
