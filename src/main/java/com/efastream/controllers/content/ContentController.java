package com.efastream.controllers.content;

import com.efastream.config.UnifiedUserDetails;
import com.efastream.models.dto.ApiResponse;
import com.efastream.models.dto.ContentResponse;
import com.efastream.models.enums.ContentType;
import com.efastream.services.ContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/content")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ContentResponse>>> listApproved(
            @RequestParam(required = false) ContentType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(contentService.getApprovedContent(type, PageRequest.of(page, size))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ContentResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(contentService.getById(id, true)));
    }

    @PostMapping("/stream/{id}")
    public ResponseEntity<ApiResponse<String>> recordStream(
            @AuthenticationPrincipal UnifiedUserDetails user,
            @PathVariable Long id) {
        contentService.recordView(user.id(), id);
        return ResponseEntity.ok(ApiResponse.success("View recorded"));
    }

    @PostMapping("/download/{id}")
    public ResponseEntity<ApiResponse<String>> recordDownload(
            @AuthenticationPrincipal UnifiedUserDetails user,
            @PathVariable Long id) {
        contentService.recordDownload(user.id(), id);
        return ResponseEntity.ok(ApiResponse.success("Download recorded"));
    }
}
