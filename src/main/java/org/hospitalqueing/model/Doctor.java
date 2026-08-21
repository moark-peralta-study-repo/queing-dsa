package org.hospitalqueing.model;

public class Doctor {
  private int doctorId;
  private int departmentId;
  private String firstName;
  private String lastName;
  private String licenseNum;
  private boolean isActive;

  public Doctor() {}

  public Doctor(
      int doctorId,
      int departmentId,
      String firstName,
      String lastName,
      String licenseNum,
      boolean isActive) {
    this.doctorId = doctorId;
    this.departmentId = departmentId;
    this.firstName = firstName;
    this.lastName = lastName;
    this.licenseNum = licenseNum;
    this.isActive = isActive;
  }

  public int getDoctorId() {
    return doctorId;
  }

  public void setDoctorId(int doctorId) {
    this.doctorId = doctorId;
  }

  public int getDepartmentId() {
    return departmentId;
  }

  public void setDepartmentId(int departmentId) {
    this.departmentId = departmentId;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getLicenseNum() {
    return licenseNum;
  }

  public void setLicenseNum(String licenseNum) {
    this.licenseNum = licenseNum;
  }

  public boolean isActive() {
    return isActive;
  }

  public void setActive(boolean isActive) {
    this.isActive = isActive;
  }
}
