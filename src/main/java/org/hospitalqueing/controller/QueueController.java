package org.hospitalqueing.controller;

import java.util.List;

import org.hospitalqueing.model.QueueEntry;
import org.hospitalqueing.service.QueueService;

public class QueueController {
  private final QueueService queueService;

  public QueueController(QueueService queueService) {
    this.queueService = queueService;
  }

  public void save(QueueEntry queueEntry) {
    queueService.save(queueEntry);
  }

  public QueueEntry findById(int queueId) {
    return queueService.findById(queueId);
  }

  public List<QueueEntry> findAll() {
    return queueService.findAll();
  }

  public void update(QueueEntry queueEntry) {
    queueService.update(queueEntry);
  }

  public void delete(int queueId) {
    queueService.delete(queueId);
  }
}
