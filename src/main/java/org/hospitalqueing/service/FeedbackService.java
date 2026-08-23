package org.hospitalqueing.service;

import java.util.List;

import org.hospitalqueing.dao.FeedbackDAO;
import org.hospitalqueing.model.Feedback;

public class FeedbackService {
  private final FeedbackDAO feedbackDAO;

  FeedbackService(FeedbackDAO feedbackDAO) {
    this.feedbackDAO = feedbackDAO;
  }

  public void createFeedback(Feedback feedback) {
    feedbackDAO.save(feedback);
  }

  public Feedback getFeedbackById(int feedbackId) {
    return feedbackDAO.findById(feedbackId);
  }

  public List<Feedback> getAllFeedback() {
    return feedbackDAO.findAll();
  }

  public List<Feedback> getFeedbackByQueue(int queueId) {
    return feedbackDAO.findByQueue(queueId);
  }

  public List<Feedback> getFeedbackByPatient(int patientId) {
    return feedbackDAO.findByPatient(patientId);
  }

  public void updateFeedback(Feedback feedback) {
    feedbackDAO.update(feedback);
  }

  public void deleteFeedback(int feedbackId) {
    feedbackDAO.delete(feedbackId);
  }
}
