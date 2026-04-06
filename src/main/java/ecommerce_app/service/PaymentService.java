package ecommerce_app.service;

import ecommerce_app.dto.request.InitiatePaymentRequest;
import ecommerce_app.dto.response.DailyCashSummary;
import ecommerce_app.dto.response.InitiatePaymentResponse;
import ecommerce_app.dto.response.PaymentStatusResponse;
import ecommerce_app.entity.Payment;

import java.time.LocalDate;

public interface PaymentService {

  /**
   * Initiates a payment for a given order. Called right after checkout or when user chooses to pay.
   */
  InitiatePaymentResponse initiate(InitiatePaymentRequest request, Long userId);

  /**
   * Polls/returns the current payment status. For COD and Cash-in-Shop this just returns DB state.
   * For BAKONG this will later call the Bakong API.
   */
  PaymentStatusResponse getStatus(Long paymentId, Long userId);

  /**
   * Confirms a payment — called by webhook handlers or staff manually. Idempotent: safe to call
   * multiple times.
   */
  void confirmPayment(Payment payment);

  /** Staff marks COD payment as collected (cash received at delivery). */
  void markCodPaid(Long orderId, Long staffUserId);

  /** Staff marks Cash-in-Shop payment as collected (customer paid at store). */
  void markCashInShopPaid(Long orderId, Long staffUserId);

  DailyCashSummary getDailyCashSummary(LocalDate date);

  DailyCashSummary getDailyCashSummaryByCashier(LocalDate date, Long cashierId);
}
