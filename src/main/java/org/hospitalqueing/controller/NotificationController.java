package org.hospitalqueing.controller;

import java.util.List;

import org.hospitalqueing.model.Notification;
import org.hospitalqueing.service.NotificationService;

public class NotificationController {
  private final NotificationService notificationService;

  public NotificationController(NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  public void createNotification(Notification notification) {
    notificationService.createNotification(notification);
  }

  public Notification getNotification(int notificationId) {
    return notificationService.getNotificationById(notificationId);
  }

  public List<Notification> getAllNotifications() {
    return notificationService.getAllNotifications();
  }

  public List<Notification> getNotificationsByPatient(int patientId) {
    return notificationService.getNotificationsByPatient(patientId);
  }

  public void markAsSent(Notification notification) {
    notificationService.markAsSent(notification);
  }

  public void updateNotification(Notification notification) {
    notificationService.updateNotification(notification);
  }

  public void deleteNotification(int notificationId) {
    notificationService.deleteNotification(notificationId);
  }
}
