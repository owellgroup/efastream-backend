package com.efastream.config;

import com.efastream.models.enums.PaymentGateway;
import com.efastream.services.PaymentGatewayService;
import com.efastream.services.impl.PaypalPaymentService;
import com.efastream.services.impl.StripePaymentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Map;

@Configuration
public class PaymentGatewayConfig {

    @Bean
    @Primary
    public PaymentGatewayService paymentGatewayService(
            @Value("${payment.gateway:STRIPE}") String gatewayName,
            StripePaymentService stripePaymentService,
            PaypalPaymentService paypalPaymentService) {
        String name = (gatewayName == null || gatewayName.isBlank()) ? "STRIPE" : gatewayName.trim().toUpperCase();
        PaymentGateway gateway = PaymentGateway.valueOf(name);
        return switch (gateway) {
            case PAYPAL -> paypalPaymentService;
            default -> stripePaymentService;
        };
    }
}
