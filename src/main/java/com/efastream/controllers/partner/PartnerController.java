package com.efastream.controllers.partner;

import com.efastream.config.UnifiedUserDetails;
import com.efastream.models.dto.*;
import com.efastream.models.entity.Partner;
import com.efastream.services.ContentService;
import com.efastream.services.PartnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/partners")
@RequiredArgsConstructor
public class PartnerController {

    private final PartnerService partnerService;
    private final ContentService contentService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<PartnerResponse>> getProfile(@AuthenticationPrincipal UnifiedUserDetails user) {
        return ResponseEntity.ok(ApiResponse.success(partnerService.getPartner(user.id())));
    }

    @PostMapping("/content")
    public ResponseEntity<ApiResponse<ContentResponse>> uploadContent(
            @AuthenticationPrincipal UnifiedUserDetails user,
            @Valid @RequestBody ContentRequest request) {
        Partner partner = partnerService.getEntityById(user.id());
        return ResponseEntity.ok(ApiResponse.success(contentService.createContent(user.id(), request, partner)));
    }

    @GetMapping("/content")
    public ResponseEntity<ApiResponse<List<ContentResponse>>> myContent(@AuthenticationPrincipal UnifiedUserDetails user) {
        return ResponseEntity.ok(ApiResponse.success(contentService.getPartnerContent(user.id())));
    }

    @GetMapping("/analytics")
    public ResponseEntity<ApiResponse<List<ContentAnalyticsResponse>>> analytics(@AuthenticationPrincipal UnifiedUserDetails user) {
        return ResponseEntity.ok(ApiResponse.success(contentService.getPartnerAnalytics(user.id())));
    }
}
