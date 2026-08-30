package org.hospitalqueing.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import org.hospitalqueing.dao.QueueEntryDAO;
import org.hospitalqueing.dao.QueueEventDAO;
import org.hospitalqueing.model.QueueEntry;
import org.hospitalqueing.model.QueueEvent;

public class QueueManagementService {
  private final QueueEntryDAO queueEntryDAO;
  private final QueueEventDAO queueEventDAO;

  public QueueManagementService(QueueEntryDAO queueEntryDAO, QueueEventDAO queueEventDAO) {
    this.queueEntryDAO = queueEntryDAO;
    this.queueEventDAO = queueEventDAO;
  }

  public QueueEntry joinQueue(QueueEntry entry) {
    String queueDate = entry.getQueueDate();
    if (queueDate == null || queueDate.isEmpty()) {
      queueDate = LocalDate.now().toString();
      entry.setQueueDate(queueDate);
    }

    int nextNumber = nextQueueNumber(entry.getDepartmentId(), queueDate);
    entry.setQueueNumber(nextNumber);

    if (entry.getPriorityType() == null || entry.getPriorityType().isEmpty()) {
      entry.setPriorityType("NORMAL");
    }
    if (entry.getStatus() == null || entry.getStatus().isEmpty()) {
      entry.setStatus("WAITING");
    }
    entry.setJoinedAt(LocalDate.now() + " " + LocalTime.now().withNano(0));

    if (entry.getQrToken() == null || entry.getQrToken().isEmpty()) {
      entry.setQrToken(UUID.randomUUID().toString());
    }

    queueEntryDAO.save(entry);

    logEvent(entry.getQueueId(), null, "JOINED", "Joined queue as number " + nextNumber);

    return entry;
  }

  public int nextQueueNumber(int departmentId, String queueDate) {
    return queueEntryDAO.findAll().stream()
            .filter(e -> e.getDepartmentId() == departmentId)
            .filter(e -> queueDate == null || queueDate.equals(e.getQueueDate()))
            .map(QueueEntry::getQueueNumber)
            .max(Comparator.naturalOrder())
            .orElse(0)
        + 1;
  }

  public boolean advance(int queueId, Consumer<QueueEntry> hook) {
    QueueEntry entry = queueEntryDAO.findById(queueId);
    if (entry == null) {
      return false;
    }

    switch (entry.getStatus()) {
      case "WAITING" -> {
        entry.setStatus("CALLED");
        entry.setCalledAt(timestamp());
        logEvent(queueId, null, "CALLED", "Called to counter");
      }
      case "CALLED" -> {
        if (hook != null) {
          hook.accept(entry);
        }
        entry.setStatus("IN_SERVICE");
        entry.setServiceStartedAt(timestamp());
        logEvent(queueId, null, "SERVING", "Service started");
      }
      case "IN_SERVICE" -> {
        entry.setStatus("COMPLETED");
        entry.setCompletedAt(timestamp());
        logEvent(queueId, null, "COMPLETED", "Queue entry completed");
      }
      default -> {
        return false;
      }
    }

    queueEntryDAO.update(entry);
    return true;
  }

  public boolean skip(int queueId) {
    QueueEntry entry = queueEntryDAO.findById(queueId);
    if (entry == null || entry.getStatus().equals("COMPLETED")) {
      return false;
    }

    entry.setStatus("SKIPPED");
    entry.setCompletedAt(timestamp());
    queueEntryDAO.update(entry);
    logEvent(queueId, null, "SKIPPED", "Entry skipped");
    return true;
  }

  /** Marks a waited-for entry as a no-show. Returns false if entry not found. */
  public boolean markNoShow(int queueId) {
    QueueEntry entry = queueEntryDAO.findById(queueId);
    if (entry == null) {
      return false;
    }

    entry.setStatus("NO_SHOW");
    entry.setCompletedAt(timestamp());
    queueEntryDAO.update(entry);
    logEvent(queueId, null, "NO_SHOW", "Patient did not show");
    return true;
  }

  public List<QueueEntry> getActiveQueue(int departmentId) {
    return queueEntryDAO.findAll().stream()
        .filter(e -> e.getDepartmentId() == departmentId)
        .filter(
            e ->
                e.getStatus().equals("WAITING")
                    || e.getStatus().equals("CALLED")
                    || e.getStatus().equals("IN_SERVICE"))
        .sorted(Comparator.comparing(QueueEntry::getJoinedAt))
        .toList();
  }

  private void logEvent(int queueId, Integer staffId, String eventType, String notes) {
    queueEventDAO.save(new QueueEvent(0, queueId, staffId, eventType, notes, LocalDateTime.now()));
  }

  private static String timestamp() {
    return LocalDate.now() + " " + LocalTime.now().withNano(0);
  }
}
