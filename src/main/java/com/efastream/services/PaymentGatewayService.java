package com.efastream.services;

import com.efastream.models.dto.PaymentRequest;
import com.efastream.models.dto.PaymentResponse;
import com.efastream.models.entity.Payment;
import com.efastream.models.entity.User;

/**
 * Payment gateway abstraction. Implementations: Stripe, PayPal, Flutterwave, Paystack.
 * Switch gateway via configuration (payment.gateway).
 */
public interface PaymentGatewayService {

    /**
     * Create a payment and return approval URL for redirect.
     */
    PaymentResponse createPayment(User user, PaymentRequest request, Payment paymentEntity);

    /**
     * Verify payment status after user returns from gateway.
     */
    Payment verifyPayment(String transactionIdOrPaymentId);

    /**
     * Refund a completed payment.
     */
    boolean refundPayment(Payment payment);
}
