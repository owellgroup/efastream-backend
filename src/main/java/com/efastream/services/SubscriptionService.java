package com.efastream.services;

import com.efastream.config.exception.BadRequestException;
import com.efastream.config.exception.ResourceNotFoundException;
import com.efastream.models.dto.SubscriptionPlanResponse;
import com.efastream.models.dto.SubscriptionResponse;
import com.efastream.models.entity.Subscription;
import com.efastream.models.entity.SubscriptionPlan;
import com.efastream.models.entity.User;
import com.efastream.models.enums.SubscriptionStatus;
import com.efastream.repositories.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanService subscriptionPlanService;
    private final UserService userService;

    public Optional<Subscription> getActiveSubscription(Long userId) {
        return subscriptionRepository.findActiveByUserId(userId, SubscriptionStatus.ACTIVE);
    }

    public boolean hasActiveSubscription(Long userId) {
        return getActiveSubscription(userId).isPresent();
    }

    public SubscriptionResponse getCurrentSubscription(Long userId) {
        Subscription sub = getActiveSubscription(userId)
                .orElseThrow(() -> new BadRequestException("No active subscription. Please subscribe to stream content."));
        return toResponse(sub);
    }

    public List<SubscriptionResponse> getSubscriptionHistory(Long userId) {
        return subscriptionRepository.findByUserIdOrderByEndDateDesc(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Subscription createSubscription(User user, SubscriptionPlan plan) {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(plan.getDurationDays());
        Subscription sub = Subscription.builder()
                .user(user)
                .plan(plan)
                .startDate(start)
                .endDate(end)
                .status(SubscriptionStatus.ACTIVE)
                .build();
        return subscriptionRepository.save(sub);
    }

    public void expireSubscriptions() {
        LocalDate today = LocalDate.now();
        subscriptionRepository.findAll().stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE && s.getEndDate().isBefore(today))
                .forEach(s -> {
                    s.setStatus(SubscriptionStatus.EXPIRED);
                    subscriptionRepository.save(s);
                });
    }

    public SubscriptionResponse toResponse(Subscription sub) {
        return SubscriptionResponse.builder()
                .id(sub.getId())
                .userId(sub.getUser().getId())
                .plan(subscriptionPlanService.toResponse(sub.getPlan()))
                .startDate(sub.getStartDate())
                .endDate(sub.getEndDate())
                .status(sub.getStatus())
                .createdAt(sub.getCreatedAt())
                .build();
    }
}
