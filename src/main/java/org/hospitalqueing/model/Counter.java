package org.hospitalqueing.model;

public class Counter {
  private int counterId;
  private int departmentId;
  private String counterName;
  private String roomName;
  private boolean isActive;

  public Counter() {}

  public Counter(
      int counterId, int departmentId, String counterName, String roomName, boolean isActive) {
    this.counterId = counterId;
    this.departmentId = departmentId;
    this.counterName = counterName;
    this.roomName = roomName;
    this.isActive = isActive;
  }

  public int getCounterId() {
    return counterId;
  }

  public void setCounterId(int counterId) {
    this.counterId = counterId;
  }

  public int getDepartmentId() {
    return departmentId;
  }

  public void setDepartmentId(int departmentId) {
    this.departmentId = departmentId;
  }

  public String getCounterName() {
    return counterName;
  }

  public void setCounterName(String counterName) {
    this.counterName = counterName;
  }

  public String getRoomName() {
    return roomName;
  }

  public void setRoomName(String roomName) {
    this.roomName = roomName;
  }

  public boolean isActive() {
    return isActive;
  }

  public void setActive(boolean isActive) {
    this.isActive = isActive;
  }
}
