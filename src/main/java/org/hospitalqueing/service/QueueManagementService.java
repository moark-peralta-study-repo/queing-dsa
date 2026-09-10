package org.hospitalqueing.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import org.hospitalqueing.dao.CounterDAO;
import org.hospitalqueing.dao.DepartmentDAO;
import org.hospitalqueing.dao.QueueEntryDAO;
import org.hospitalqueing.dao.QueueEventDAO;
import org.hospitalqueing.dao.ServiceDAO;
import org.hospitalqueing.model.Counter;
import org.hospitalqueing.model.Department;
import org.hospitalqueing.model.QueueEntry;
import org.hospitalqueing.model.QueueEvent;
import org.hospitalqueing.model.Service;
import org.hospitalqueing.model.TicketStatus;

public class QueueManagementService {
  private final QueueEntryDAO queueEntryDAO;
  private final QueueEventDAO queueEventDAO;
  private final ServiceDAO serviceDAO;
  private final DepartmentDAO departmentDAO;
  private final CounterDAO counterDAO;

  public QueueManagementService(
      QueueEntryDAO queueEntryDAO,
      QueueEventDAO queueEventDAO,
      ServiceDAO serviceDAO,
      DepartmentDAO departmentDAO,
      CounterDAO counterDAO) {
    this.queueEntryDAO = queueEntryDAO;
    this.queueEventDAO = queueEventDAO;
    this.serviceDAO = serviceDAO;
    this.departmentDAO = departmentDAO;
    this.counterDAO = counterDAO;
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
      entry.setPriorityType("REGULAR");
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
        .sorted(
            Comparator.comparingInt((QueueEntry e) -> priorityRank(e.getPriorityType()))
                .thenComparing(QueueEntry::getJoinedAt, Comparator.nullsFirst(Comparator.naturalOrder())))
        .toList();
  }

  public static int priorityRank(String priorityType) {
    return switch (priorityType == null ? "" : priorityType) {
      case "EMERGENCY" -> 0;
      case "APPOINTMENT" -> 1;
      case "PWD" -> 2;
      case "SENIOR" -> 3;
      default -> 4;
    };
  }

  public TicketStatus getTicketStatus(String qrToken) {
    QueueEntry entry = queueEntryDAO.findByQrToken(qrToken);
    if (entry == null) {
      return null;
    }

    Department department = departmentDAO.findById(entry.getDepartmentId());
    Service service = serviceDAO.findById(entry.getServiceId());
    Counter counter = entry.getCounterId() != null ? counterDAO.findById(entry.getCounterId()) : null;

    List<QueueEntry> active = getActiveQueue(entry.getDepartmentId());

    int ahead = 0;
    boolean waiting = "WAITING".equals(entry.getStatus());
    if (waiting) {
      for (QueueEntry e : active) {
        if (e.getQueueId() == entry.getQueueId()) {
          break;
        }
        ahead++;
      }
    }

    Integer currentNumber = null;
    for (QueueEntry e : active) {
      if ("IN_SERVICE".equals(e.getStatus()) || "CALLED".equals(e.getStatus())) {
        currentNumber = e.getQueueNumber();
        break;
      }
    }

    int avgMinutes =
        service != null && service.getAvgServiceMinutes() > 0 ? service.getAvgServiceMinutes() : 10;
    int eta = waiting ? ahead * avgMinutes : 0;

    return new TicketStatus(
        entry.getQrToken(),
        entry.getQueueId(),
        entry.getQueueNumber(),
        department != null ? department.getDepartmentName() : null,
        service != null ? service.getServiceName() : null,
        entry.getStatus(),
        entry.getPriorityType(),
        currentNumber,
        ahead,
        eta,
        counter != null ? counter.getCounterName() : null,
        entry.getJoinedAt(),
        entry.getCalledAt());
  }

  private void logEvent(int queueId, Integer staffId, String eventType, String notes) {
    queueEventDAO.save(new QueueEvent(0, queueId, staffId, eventType, notes, LocalDateTime.now()));
  }

  private static String timestamp() {
    return LocalDate.now() + " " + LocalTime.now().withNano(0);
  }
}
