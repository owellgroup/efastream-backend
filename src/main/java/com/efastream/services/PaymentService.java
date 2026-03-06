package com.efastream.services;

import com.efastream.config.exception.BadRequestException;
import com.efastream.models.dto.PaymentRequest;
import com.efastream.models.dto.PaymentResponse;
import com.efastream.models.entity.Payment;
import com.efastream.models.entity.Subscription;
import com.efastream.models.entity.SubscriptionPlan;
import com.efastream.models.entity.User;
import com.efastream.models.enums.PaymentGateway;
import com.efastream.models.enums.PaymentStatus;
import com.efastream.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentGatewayService paymentGatewayService;
    private final SubscriptionPlanService subscriptionPlanService;
    private final SubscriptionService subscriptionService;
    private final UserService userService;
    private final EmailService emailService;

    @Value("${payment.gateway:STRIPE}")
    private String gatewayName;

    @Transactional
    public PaymentResponse createPayment(Long userId, PaymentRequest request) {
        User user = userService.getEntityById(userId);
        SubscriptionPlan plan = subscriptionPlanService.getEntityById(request.getPlanId());
        String currency = request.getCurrency() != null && !request.getCurrency().isBlank() ? request.getCurrency() : "USD";
        if (request.getAmount().compareTo(plan.getPrice()) < 0) {
            throw new BadRequestException("Amount is less than plan price");
        }
        Payment payment = Payment.builder()
                .user(user)
                .plan(plan)
                .amount(request.getAmount())
                .currency(currency)
                .status(PaymentStatus.PENDING)
                .gateway(PaymentGateway.valueOf(gatewayName.toUpperCase()))
                .build();
        payment = paymentRepository.save(payment);
        return paymentGatewayService.createPayment(user, request, payment);
    }

    @Transactional
    public PaymentResponse verifyAndActivateSubscription(String transactionIdOrPaymentId) {
        Payment payment = paymentGatewayService.verifyPayment(transactionIdOrPaymentId);
        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            return toResponse(payment);
        }
        payment.setStatus(PaymentStatus.COMPLETED);
        SubscriptionPlan plan = payment.getPlan();
        if (plan == null) {
            throw new BadRequestException("Payment has no plan associated");
        }
        Subscription sub = subscriptionService.createSubscription(payment.getUser(), plan);
        payment.setSubscription(sub);
        paymentRepository.save(payment);
        emailService.sendSubscriptionSuccess(
                payment.getUser().getEmail(),
                payment.getUser().getFirstName(),
                plan.getName(),
                sub.getEndDate().toString());
        return toResponse(payment);
    }

    public List<PaymentResponse> getPaymentHistory(Long userId) {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private PaymentResponse toResponse(Payment p) {
        return PaymentResponse.builder()
                .id(p.getId())
                .userId(p.getUser().getId())
                .amount(p.getAmount())
                .currency(p.getCurrency())
                .status(p.getStatus())
                .gateway(p.getGateway())
                .transactionId(p.getTransactionId())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
