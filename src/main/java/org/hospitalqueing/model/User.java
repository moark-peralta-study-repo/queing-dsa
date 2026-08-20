package org.hospitalqueing.model;

import java.time.LocalDateTime;

public class User {
  private int userId;
  private String username;
  private String passwordHash;
  private int roleId;
  private boolean isActive;
  private LocalDateTime createdAt;

  public User() {}

  public User(
      int userId,
      String username,
      String passwordHash,
      int roleId,
      boolean isActive,
      LocalDateTime createdAt) {
    this.userId = userId;
    this.username = username;
    this.passwordHash = passwordHash;
    this.roleId = roleId;
    this.isActive = isActive;
    this.createdAt = createdAt;
  }

  public int getUserId() {
    return userId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public int getRoleId() {
    return roleId;
  }

  public void setRoleId(int roleId) {
    this.roleId = roleId;
  }

  public boolean isActive() {
    return isActive;
  }

  public void setActive(boolean isActive) {
    this.isActive = isActive;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
