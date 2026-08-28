package org.hospitalqueing.controller;

import java.util.List;

import org.hospitalqueing.model.Feedback;
import org.hospitalqueing.service.FeedbackService;

public class FeedbackController {
  private final FeedbackService feedbackService;

  public FeedbackController(FeedbackService feedbackService) {
    this.feedbackService = feedbackService;
  }

  public void createFeedback(Feedback feedback) {
    feedbackService.createFeedback(feedback);
  }

  public Feedback getFeedback(int feedbackId) {
    return feedbackService.getFeedbackById(feedbackId);
  }

  public List<Feedback> getAllFeedback() {
    return feedbackService.getAllFeedback();
  }

  public List<Feedback> getFeedbackByQueue(int queueId) {
    return feedbackService.getFeedbackByQueue(queueId);
  }

  public List<Feedback> getFeedbackByPatient(int patientId) {
    return feedbackService.getFeedbackByPatient(patientId);
  }

  public void updateFeedback(Feedback feedback) {
    feedbackService.updateFeedback(feedback);
  }

  public void deleteFeedback(int feedbackId) {
    feedbackService.deleteFeedback(feedbackId);
  }
}
