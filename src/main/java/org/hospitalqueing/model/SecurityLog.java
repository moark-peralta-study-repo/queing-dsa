package org.hospitalqueing.model;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * One row in the security_logs table: a recorded authentication event (login success/failure or
 * logout). Timestamps are stored as "yyyy-MM-dd HH:mm:ss" (same vocabulary as the other tables);
 * the client host is best-effort (empty string when InetAddress.getLocalHost() fails, e.g. on
 * machines without a resolvable hostname).
 */
public class SecurityLog {

  /** Recorded event types (the {@code action} column). */
  public static final String LOGIN_SUCCESS = "LOGIN_SUCCESS";
  public static final String LOGIN_FAIL = "LOGIN_FAIL";
  public static final String LOGOUT = "LOGOUT";

  private static final DateTimeFormatter CREATED_FMT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private int id;
  private String action;
  private String username;
  private String role;
  private boolean success;
  private String host;
  private String createdAt;

  public SecurityLog() {}

  /** New (not-yet-persisted) event: stamps the client host and creation time. */
  public SecurityLog(String action, String username, String role, boolean success) {
    this.action = action;
    this.username = username;
    this.role = role == null ? "" : role;
    this.success = success;
    this.host = resolveHost();
    this.createdAt = LocalDateTime.now().format(CREATED_FMT);
  }

  private static String resolveHost() {
    try {
      return InetAddress.getLocalHost().getHostAddress();
    } catch (Exception e) {
      return "";
    }
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public boolean isSuccess() {
    return success;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public String getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
  }
}
