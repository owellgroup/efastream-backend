package com.efastream.controllers.user;

import com.efastream.config.UnifiedUserDetails;
import com.efastream.models.dto.*;
import com.efastream.services.ContentService;
import com.efastream.services.SubscriptionService;
import com.efastream.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final SubscriptionService subscriptionService;
    private final ContentService contentService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(@AuthenticationPrincipal UnifiedUserDetails user) {
        return ResponseEntity.ok(ApiResponse.success(userService.getProfile(user.id())));
    }

    @GetMapping("/me/subscription")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getCurrentSubscription(@AuthenticationPrincipal UnifiedUserDetails user) {
        return ResponseEntity.ok(ApiResponse.success(subscriptionService.getCurrentSubscription(user.id())));
    }

    @GetMapping("/me/subscription/history")
    public ResponseEntity<ApiResponse<List<SubscriptionResponse>>> getSubscriptionHistory(@AuthenticationPrincipal UnifiedUserDetails user) {
        return ResponseEntity.ok(ApiResponse.success(subscriptionService.getSubscriptionHistory(user.id())));
    }

    @GetMapping("/me/history")
    public ResponseEntity<ApiResponse<List<ContentResponse>>> getViewingHistory(
            @AuthenticationPrincipal UnifiedUserDetails user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<ContentResponse> list = contentService.getViewingHistory(user.id(), PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(list));
    }
}
