package org.hospitalqueing.controller;

import java.util.List;

import org.hospitalqueing.model.Payment;
import org.hospitalqueing.service.PaymentService;

public class PaymentController {
  private final PaymentService paymentService;

  public PaymentController(PaymentService paymentService) {
    this.paymentService = paymentService;
  }

  public void createPayment(Payment payment) {
    paymentService.createPayment(payment);
  }

  public Payment getPayment(int paymentId) {
    return paymentService.getPaymentById(paymentId);
  }

  public Payment getPaymentByQueue(int queueId) {
    return paymentService.getPaymentByQueue(queueId);
  }

  public List<Payment> getAllPayments() {
    return paymentService.getAllPayments();
  }

  public void markAsPaid(Payment payment) {
    paymentService.markAsPaid(payment);
  }

  public void updatePayment(Payment payment) {
    paymentService.updatePayment(payment);
  }

  public void deletePayment(int paymentId) {
    paymentService.deletePayment(paymentId);
  }
}
