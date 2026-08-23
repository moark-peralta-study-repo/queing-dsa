package org.hospitalqueing.service;

import java.util.List;

import org.hospitalqueing.dao.QueueEventDAO;
import org.hospitalqueing.model.QueueEvent;

public class QueueEventService {
  private final QueueEventDAO queueEventDAO;

  QueueEventService(QueueEventDAO queueEventDAO) {
    this.queueEventDAO = queueEventDAO;
  }

  public void createQueueEvent(QueueEvent queueEvent) {
    queueEventDAO.save(queueEvent);
  }

  public QueueEvent getQueueEventById(int eventId) {
    return queueEventDAO.findById(eventId);
  }

  public List<QueueEvent> getAllQueueEvents() {
    return queueEventDAO.findAll();
  }

  public void updateQueueEvent(QueueEvent queueEvent) {
    queueEventDAO.update(queueEvent);
  }

  public void deleteQueueEvent(int eventId) {
    queueEventDAO.delete(eventId);
  }
}
