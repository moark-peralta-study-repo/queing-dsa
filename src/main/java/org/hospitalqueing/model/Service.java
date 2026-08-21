package org.hospitalqueing.model;

public class Service {
  private int serviceId;
  private int departmendId;
  private String serviceName;
  private int avgServiceMinutes;
  private boolean isActive;

  public Service() {}

  public Service(
      int serviceId,
      int departmendId,
      String serviceName,
      int avgServiceMinutes,
      boolean isActive) {
    this.serviceId = serviceId;
    this.departmendId = departmendId;
    this.serviceName = serviceName;
    this.avgServiceMinutes = avgServiceMinutes;
    this.isActive = isActive;
  }

  public int getServiceId() {
    return serviceId;
  }

  public void setServiceId(int serviceId) {
    this.serviceId = serviceId;
  }

  public int getDepartmendId() {
    return departmendId;
  }

  public void setDepartmendId(int departmendId) {
    this.departmendId = departmendId;
  }

  public String getServiceName() {
    return serviceName;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  public int getAvgServiceMinutes() {
    return avgServiceMinutes;
  }

  public void setAvgServiceMinutes(int avgServiceMinutes) {
    this.avgServiceMinutes = avgServiceMinutes;
  }

  public boolean isActive() {
    return isActive;
  }

  public void setActive(boolean isActive) {
    this.isActive = isActive;
  }
}
