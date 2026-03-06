package com.efastream.models.dto;

import com.efastream.models.enums.SubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResponse {

    private Long id;
    private Long userId;
    private SubscriptionPlanResponse plan;
    private LocalDate startDate;
    private LocalDate endDate;
    private SubscriptionStatus status;
    private Instant createdAt;
}
