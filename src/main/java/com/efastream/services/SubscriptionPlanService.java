package com.efastream.services;

import com.efastream.config.exception.ResourceNotFoundException;
import com.efastream.models.dto.SubscriptionPlanResponse;
import com.efastream.models.entity.SubscriptionPlan;
import com.efastream.repositories.SubscriptionPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubscriptionPlanService {

    private final SubscriptionPlanRepository subscriptionPlanRepository;

    public List<SubscriptionPlanResponse> getActivePlans() {
        return subscriptionPlanRepository.findByActiveTrue().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<SubscriptionPlanResponse> getAllPlans() {
        return subscriptionPlanRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public SubscriptionPlan getEntityById(Long id) {
        return subscriptionPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SubscriptionPlan", id));
    }

    @Transactional
    public SubscriptionPlanResponse create(String name, BigDecimal price, int durationDays) {
        SubscriptionPlan plan = SubscriptionPlan.builder()
                .name(name)
                .price(price)
                .durationDays(durationDays)
                .active(true)
                .build();
        plan = subscriptionPlanRepository.save(plan);
        return toResponse(plan);
    }

    @Transactional
    public SubscriptionPlanResponse update(Long id, String name, BigDecimal price, Integer durationDays, Boolean active) {
        SubscriptionPlan plan = getEntityById(id);
        if (name != null) plan.setName(name);
        if (price != null) plan.setPrice(price);
        if (durationDays != null) plan.setDurationDays(durationDays);
        if (active != null) plan.setActive(active);
        plan = subscriptionPlanRepository.save(plan);
        return toResponse(plan);
    }

    @Transactional
    public void delete(Long id) {
        if (!subscriptionPlanRepository.existsById(id)) throw new ResourceNotFoundException("SubscriptionPlan", id);
        subscriptionPlanRepository.deleteById(id);
    }

    public SubscriptionPlanResponse toResponse(SubscriptionPlan plan) {
        return SubscriptionPlanResponse.builder()
                .id(plan.getId())
                .name(plan.getName())
                .price(plan.getPrice())
                .durationDays(plan.getDurationDays())
                .active(plan.isActive())
                .build();
    }
}
