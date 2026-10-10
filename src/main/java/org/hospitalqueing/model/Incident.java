package org.hospitalqueing.model;

import java.time.LocalDateTime;

/**
 * An admin-reported incident (security-flow expansion): something that happened on the floor —
 * a complaint, a lost item, a safety issue — tracked from OPEN through IN_PROGRESS to RESOLVED.
 *
 * <p>Stored in the {@code incidents} table (SQLite); severity and status are persisted as their
 * enum names. {@code resolvedAt} is stamped when the status reaches RESOLVED.
 */
public class Incident {
  public enum Severity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
  }

  public enum Status {
    OPEN,
    IN_PROGRESS,
    RESOLVED
  }

  private int id;
  private String title;
  private String description;
  private Severity severity;
  private Status status;
  private String reportedBy;
  private LocalDateTime createdAt;
  private LocalDateTime resolvedAt;

  public Incident() {}

  public Incident(
      int id,
      String title,
      String description,
      Severity severity,
      Status status,
      String reportedBy,
      LocalDateTime createdAt,
      LocalDateTime resolvedAt) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.severity = severity;
    this.status = status;
    this.reportedBy = reportedBy;
    this.createdAt = createdAt;
    this.resolvedAt = resolvedAt;
  }

  /** The next status in the OPEN -> IN_PROGRESS -> RESOLVED cycle (null once RESOLVED). */
  public static Status nextStatus(Status current) {
    if (current == null) {
      return Status.OPEN;
    }
    switch (current) {
      case OPEN:
        return Status.IN_PROGRESS;
      case IN_PROGRESS:
        return Status.RESOLVED;
      case RESOLVED:
        return null;
    }
    return null;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Severity getSeverity() {
    return severity;
  }

  public void setSeverity(Severity severity) {
    this.severity = severity;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public String getReportedBy() {
    return reportedBy;
  }

  public void setReportedBy(String reportedBy) {
    this.reportedBy = reportedBy;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getResolvedAt() {
    return resolvedAt;
  }

  public void setResolvedAt(LocalDateTime resolvedAt) {
    this.resolvedAt = resolvedAt;
  }
}
