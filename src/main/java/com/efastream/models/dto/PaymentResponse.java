package com.efastream.models.dto;

import com.efastream.models.enums.PaymentGateway;
import com.efastream.models.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private Long id;
    private Long userId;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private PaymentGateway gateway;
    private String transactionId;
    private String approvalUrl;  // for redirect to gateway
    private Instant createdAt;
}
