package com.efastream.services.impl;

import com.efastream.config.exception.BadRequestException;
import com.efastream.models.dto.PaymentRequest;
import com.efastream.models.dto.PaymentResponse;
import com.efastream.models.entity.Payment;
import com.efastream.models.entity.User;
import com.efastream.models.enums.PaymentStatus;
import com.efastream.repositories.PaymentRepository;
import com.efastream.services.PaymentGatewayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Currency;

@Service
@Slf4j
@RequiredArgsConstructor
public class StripePaymentService implements PaymentGatewayService {

    private final PaymentRepository paymentRepository;

    @Value("${payment.stripe.api-key:}")
    private String stripeApiKey;

    @Override
    public PaymentResponse createPayment(User user, PaymentRequest request, Payment paymentEntity) {
        if (stripeApiKey == null || stripeApiKey.isBlank() || stripeApiKey.startsWith("sk_test")) {
            // Mock for development: return a fake approval URL
            paymentEntity.setTransactionId("stripe_mock_" + paymentEntity.getId());
            paymentRepository.save(paymentEntity);
            return PaymentResponse.builder()
                    .id(paymentEntity.getId())
                    .userId(user.getId())
                    .amount(paymentEntity.getAmount())
                    .currency(paymentEntity.getCurrency())
                    .status(paymentEntity.getStatus())
                    .gateway(paymentEntity.getGateway())
                    .transactionId(paymentEntity.getTransactionId())
                    .approvalUrl(request.getReturnUrl() != null ? request.getReturnUrl() + "?token=mock_stripe_" + paymentEntity.getId() : null)
                    .createdAt(paymentEntity.getCreatedAt())
                    .build();
        }
        // Real Stripe integration would use Stripe SDK here
        throw new BadRequestException("Stripe API key not configured. Use mock or set STRIPE_API_KEY.");
    }

    @Override
    public Payment verifyPayment(String transactionIdOrPaymentId) {
        return paymentRepository.findByTransactionId(transactionIdOrPaymentId)
                .orElseThrow(() -> new BadRequestException("Payment not found"));
    }

    @Override
    public boolean refundPayment(Payment payment) {
        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new BadRequestException("Only completed payments can be refunded");
        }
        payment.setStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(payment);
        return true;
    }
}
