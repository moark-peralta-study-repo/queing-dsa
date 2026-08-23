package org.hospitalqueing.service;

import java.util.List;

import org.hospitalqueing.dao.NotificationDAO;
import org.hospitalqueing.model.Notification;

public class NotificationService {
  private final NotificationDAO notificationDAO;

  NotificationService(NotificationDAO notificationDAO) {
    this.notificationDAO = notificationDAO;
  }

  public void createNotification(Notification notification) {
    notificationDAO.save(notification);
  }

  public Notification getNotificationById(int notificationId) {
    return notificationDAO.findById(notificationId);
  }

  public List<Notification> getAllNotifications() {
    return notificationDAO.findAll();
  }

  public List<Notification> getNotificationsByPatient(int patientId) {
    return notificationDAO.findByPatient(patientId);
  }

  public void markAsSent(Notification notification) {
    notification.setStatus("SENT");
    notification.setSentAt(java.time.LocalDateTime.now());
    notificationDAO.update(notification);
  }

  public void updateNotification(Notification notification) {
    notificationDAO.update(notification);
  }

  public void deleteNotification(int notificationId) {
    notificationDAO.delete(notificationId);
  }
}
