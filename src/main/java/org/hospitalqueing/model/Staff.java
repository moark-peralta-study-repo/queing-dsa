package org.hospitalqueing.model;

public class Staff {
  private int staffId;
  private int userId;

  private String firstName;
  private String lastName;

  private Integer departmentId;

  public Staff() {}

  public Staff(int staffId, int userId, String firstName, String lastName, Integer departmentId) {
    this.staffId = staffId;
    this.userId = userId;
    this.firstName = firstName;
    this.lastName = lastName;
    this.departmentId = departmentId;
  }

  public int getStaffId() {
    return staffId;
  }

  public void setStaffId(int staffId) {
    this.staffId = staffId;
  }

  public int getUserId() {
    return userId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
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

  public Integer getDepartmentId() {
    return departmentId;
  }

  public void setDepartmentId(Integer departmentId) {
    this.departmentId = departmentId;
  }
}
