package org.hospitalqueing.service;

import java.util.List;

import org.hospitalqueing.dao.QueueEntryDAO;
import org.hospitalqueing.model.QueueEntry;

public class QueueService {
  private final QueueEntryDAO queueEntryDAO;

  QueueService(QueueEntryDAO queueEntryDAO) {
    this.queueEntryDAO = queueEntryDAO;
  }

  public void save(QueueEntry queueEntry) {
    queueEntryDAO.save(queueEntry);
  }

  public QueueEntry findById(int queueId) {
    return queueEntryDAO.findById(queueId);
  }

  public List<QueueEntry> findAll() {
    return queueEntryDAO.findAll();
  }

  public void update(QueueEntry queueEntry) {
    queueEntryDAO.update(queueEntry);
  }

  public void delete(int queueId) {
    queueEntryDAO.delete(queueId);
  }
}
