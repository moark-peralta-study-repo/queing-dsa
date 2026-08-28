package org.hospitalqueing.controller;

import java.util.List;

import org.hospitalqueing.model.QueueEvent;
import org.hospitalqueing.service.QueueEventService;

public class QueueEventController {
  private final QueueEventService queueEventService;

  public QueueEventController(QueueEventService queueEventService) {
    this.queueEventService = queueEventService;
  }

  public void createQueueEvent(QueueEvent queueEvent) {
    queueEventService.createQueueEvent(queueEvent);
  }

  public QueueEvent getQueueEvent(int eventId) {
    return queueEventService.getQueueEventById(eventId);
  }

  public List<QueueEvent> getAllQueueEvents() {
    return queueEventService.getAllQueueEvents();
  }

  public void updateQueueEvent(QueueEvent queueEvent) {
    queueEventService.updateQueueEvent(queueEvent);
  }

  public void deleteQueueEvent(int eventId) {
    queueEventService.deleteQueueEvent(eventId);
  }
}
