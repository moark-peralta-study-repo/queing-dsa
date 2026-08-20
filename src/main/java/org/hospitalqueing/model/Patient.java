package org.hospitalqueing.model;

public class Patient {
  private int patientId;
  private int userId;

  private String firstName;
  private String lastName;
  private String middleName;
  private String birthDate;
  private String sex;
  private String phone;

  public Patient() {}

  public Patient(
      int patientId,
      int userId,
      String firstName,
      String lastName,
      String middleName,
      String birthDate,
      String sex,
      String phone) {
    this.patientId = patientId;
    this.userId = userId;
    this.firstName = firstName;
    this.lastName = lastName;
    this.middleName = middleName;
    this.birthDate = birthDate;
    this.sex = sex;
    this.phone = phone;
  }

  public void setPatientId(int patientId) {
    this.patientId = patientId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public void setMiddleName(String middleName) {
    this.middleName = middleName;
  }

  public void setBirthDate(String birthDate) {
    this.birthDate = birthDate;
  }

  public void setSex(String sex) {
    this.sex = sex;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public int getPatientId() {
    return patientId;
  }

  public int getUserId() {
    return userId;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getMiddleName() {
    return middleName;
  }

  public String getBirthDate() {
    return birthDate;
  }

  public String getSex() {
    return sex;
  }

  public String getPhone() {
    return phone;
  }
}
