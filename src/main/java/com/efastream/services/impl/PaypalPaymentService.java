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

@Service
@Slf4j
@RequiredArgsConstructor
public class PaypalPaymentService implements PaymentGatewayService {

    private final PaymentRepository paymentRepository;

    @Value("${payment.paypal.client-id:}")
    private String clientId;

    @Override
    public PaymentResponse createPayment(User user, PaymentRequest request, Payment paymentEntity) {
        if (clientId == null || clientId.isBlank() || "xxx".equals(clientId)) {
            paymentEntity.setTransactionId("paypal_mock_" + paymentEntity.getId());
            paymentRepository.save(paymentEntity);
            return PaymentResponse.builder()
                    .id(paymentEntity.getId())
                    .userId(user.getId())
                    .amount(paymentEntity.getAmount())
                    .currency(paymentEntity.getCurrency())
                    .status(paymentEntity.getStatus())
                    .gateway(paymentEntity.getGateway())
                    .transactionId(paymentEntity.getTransactionId())
                    .approvalUrl(request.getReturnUrl() != null ? request.getReturnUrl() + "?token=mock_paypal_" + paymentEntity.getId() : null)
                    .createdAt(paymentEntity.getCreatedAt())
                    .build();
        }
        throw new BadRequestException("PayPal not configured. Use mock or set payment.paypal.client-id.");
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
