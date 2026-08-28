package org.hospitalqueing.service;

import java.util.List;

import org.hospitalqueing.dao.PaymentDAO;
import org.hospitalqueing.model.Payment;

public class PaymentService {
  private final PaymentDAO paymentDAO;

  public PaymentService(PaymentDAO paymentDAO) {
    this.paymentDAO = paymentDAO;
  }

  public void createPayment(Payment payment) {
    paymentDAO.save(payment);
  }

  public Payment getPaymentById(int paymentId) {
    return paymentDAO.findById(paymentId);
  }

  public Payment getPaymentByQueue(int queueId) {
    return paymentDAO.findByQueue(queueId);
  }

  public List<Payment> getAllPayments() {
    return paymentDAO.findAll();
  }

  public void markAsPaid(Payment payment) {
    payment.setStatus("PAID");
    payment.setPaidAt(java.time.LocalDateTime.now());
    paymentDAO.update(payment);
  }

  public void updatePayment(Payment payment) {
    paymentDAO.update(payment);
  }

  public void deletePayment(int paymentId) {
    paymentDAO.delete(paymentId);
  }
}
