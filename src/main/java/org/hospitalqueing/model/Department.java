package org.hospitalqueing.model;

public class Department {
  private int departmentId;
  private String departmentName;
  private boolean isActive;

  public Department() {}

  public Department(int departmentId, String departmentName, boolean isActive) {
    this.departmentId = departmentId;
    this.departmentName = departmentName;
    this.isActive = isActive;
  }

  public int getDepartmentId() {
    return departmentId;
  }

  public void setDepartmentId(int departmentId) {
    this.departmentId = departmentId;
  }

  public String getDepartmentName() {
    return departmentName;
  }

  public void setDepartmentName(String departmentName) {
    this.departmentName = departmentName;
  }

  public boolean getIsActive() {
    return isActive;
  }

  public void setIsActive(boolean isActive) {
    this.isActive = isActive;
  }
}
