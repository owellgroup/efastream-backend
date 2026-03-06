package com.efastream.controllers.admin;

import com.efastream.models.dto.*;
import com.efastream.services.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;
    private final PartnerService partnerService;
    private final SubscriptionPlanService subscriptionPlanService;
    private final HeroSectionService heroSectionService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> dashboard() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getDashboard()));
    }

    // Users
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> listUsers() {
        return ResponseEntity.ok(ApiResponse.success(userService.getAll()));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getProfile(id)));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        String firstName = (String) body.get("firstName");
        String lastName = (String) body.get("lastName");
        Boolean enabled = body.get("enabled") != null ? (Boolean) body.get("enabled") : null;
        return ResponseEntity.ok(ApiResponse.success(userService.update(id, firstName, lastName, enabled)));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted"));
    }

    // Partners
    @GetMapping("/partners")
    public ResponseEntity<ApiResponse<List<PartnerResponse>>> listPartners() {
        return ResponseEntity.ok(ApiResponse.success(partnerService.getAllPartners()));
    }

    @PostMapping("/partners")
    public ResponseEntity<ApiResponse<PartnerResponse>> createPartner(@Valid @RequestBody PartnerRequest request) {
        return ResponseEntity.ok(ApiResponse.success(partnerService.createPartner(request)));
    }

    @GetMapping("/partners/{id}")
    public ResponseEntity<ApiResponse<PartnerResponse>> getPartner(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(partnerService.getPartner(id)));
    }

    @PutMapping("/partners/{id}")
    public ResponseEntity<ApiResponse<PartnerResponse>> updatePartner(@PathVariable Long id, @Valid @RequestBody PartnerRequest request) {
        return ResponseEntity.ok(ApiResponse.success(partnerService.updatePartner(id, request)));
    }

    @DeleteMapping("/partners/{id}")
    public ResponseEntity<ApiResponse<String>> deletePartner(@PathVariable Long id) {
        partnerService.deletePartner(id);
        return ResponseEntity.ok(ApiResponse.success("Partner deleted"));
    }

    // Content moderation
    @GetMapping("/content/pending")
    public ResponseEntity<ApiResponse<List<ContentResponse>>> pendingContent() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getPendingContent()));
    }

    @PostMapping("/content/{id}/approve")
    public ResponseEntity<ApiResponse<ContentResponse>> approveContent(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(adminService.approveContent(id)));
    }

    @PostMapping("/content/{id}/reject")
    public ResponseEntity<ApiResponse<ContentResponse>> rejectContent(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : null;
        return ResponseEntity.ok(ApiResponse.success(adminService.rejectContent(id, reason)));
    }

    // Subscription plans
    @GetMapping("/plans")
    public ResponseEntity<ApiResponse<List<SubscriptionPlanResponse>>> listPlans() {
        return ResponseEntity.ok(ApiResponse.success(subscriptionPlanService.getAllPlans()));
    }

    @PostMapping("/plans")
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> createPlan(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        BigDecimal price = new BigDecimal(body.get("price").toString());
        int durationDays = ((Number) body.get("durationDays")).intValue();
        return ResponseEntity.ok(ApiResponse.success(subscriptionPlanService.create(name, price, durationDays)));
    }

    @PutMapping("/plans/{id}")
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> updatePlan(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        BigDecimal price = body.get("price") != null ? new BigDecimal(body.get("price").toString()) : null;
        Integer durationDays = body.get("durationDays") != null ? ((Number) body.get("durationDays")).intValue() : null;
        Boolean active = body.get("active") != null ? (Boolean) body.get("active") : null;
        return ResponseEntity.ok(ApiResponse.success(subscriptionPlanService.update(id, name, price, durationDays, active)));
    }

    @DeleteMapping("/plans/{id}")
    public ResponseEntity<ApiResponse<String>> deletePlan(@PathVariable Long id) {
        subscriptionPlanService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Plan deleted"));
    }

    // Hero sections
    @GetMapping("/hero")
    public ResponseEntity<ApiResponse<List<HeroSectionResponse>>> listHero() {
        return ResponseEntity.ok(ApiResponse.success(heroSectionService.getAll()));
    }

    @PostMapping("/hero")
    public ResponseEntity<ApiResponse<HeroSectionResponse>> createHero(@Valid @RequestBody HeroSectionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(heroSectionService.create(request)));
    }

    @PutMapping("/hero/{id}")
    public ResponseEntity<ApiResponse<HeroSectionResponse>> updateHero(@PathVariable Long id, @Valid @RequestBody HeroSectionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(heroSectionService.update(id, request)));
    }

    @DeleteMapping("/hero/{id}")
    public ResponseEntity<ApiResponse<String>> deleteHero(@PathVariable Long id) {
        heroSectionService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Hero section deleted"));
    }
}
