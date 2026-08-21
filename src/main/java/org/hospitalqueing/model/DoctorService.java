package org.hospitalqueing.model;

public class DoctorService {
  private int doctorId;
  private int serviceId;

  public DoctorService() {}

  public DoctorService(int doctorId, int serviceId) {
    this.doctorId = doctorId;
    this.serviceId = serviceId;
  }

  public int getDoctorId() {
    return doctorId;
  }

  public void setDoctorId(int doctorId) {
    this.doctorId = doctorId;
  }

  public int getServiceId() {
    return serviceId;
  }

  public void setServiceId(int serviceId) {
    this.serviceId = serviceId;
  }
}
