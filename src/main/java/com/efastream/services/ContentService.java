package com.efastream.services;

import com.efastream.config.exception.BadRequestException;
import com.efastream.config.exception.ResourceNotFoundException;
import com.efastream.models.dto.ContentAnalyticsResponse;
import com.efastream.models.dto.ContentRequest;
import com.efastream.models.dto.ContentResponse;
import com.efastream.models.entity.Content;
import com.efastream.models.entity.ContentDownload;
import com.efastream.models.entity.ContentView;
import com.efastream.models.entity.Partner;
import com.efastream.models.enums.ContentStatus;
import com.efastream.models.enums.ContentType;
import com.efastream.models.entity.User;
import com.efastream.repositories.ContentDownloadRepository;
import com.efastream.repositories.ContentRepository;
import com.efastream.repositories.ContentViewRepository;
import com.efastream.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContentService {

    private final ContentRepository contentRepository;
    private final ContentViewRepository contentViewRepository;
    private final ContentDownloadRepository contentDownloadRepository;
    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;

    public Content getEntityById(Long id) {
        return contentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Content", id));
    }

    @Transactional
    public ContentResponse createContent(Long partnerId, ContentRequest request, Partner partner) {
        Content content = Content.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .thumbnail(request.getThumbnail())
                .videoUrl(request.getVideoUrl())
                .audioUrl(request.getAudioUrl())
                .contentType(request.getContentType())
                .status(ContentStatus.PENDING)
                .partner(partner)
                .viewsCount(0)
                .downloadsCount(0)
                .build();
        content = contentRepository.save(content);
        return toResponse(content);
    }

    public ContentResponse getById(Long id, boolean requireApproved) {
        Content content = getEntityById(id);
        if (requireApproved && content.getStatus() != ContentStatus.APPROVED) {
            throw new ResourceNotFoundException("Content", id);
        }
        return toResponse(content);
    }

    public Page<ContentResponse> getApprovedContent(ContentType contentType, Pageable pageable) {
        Page<Content> page = contentType != null
                ? contentRepository.findByStatusAndContentType(ContentStatus.APPROVED, contentType, pageable)
                : contentRepository.findByStatus(ContentStatus.APPROVED, pageable);
        return page.map(ContentService::toResponse);
    }

    public List<ContentResponse> getPartnerContent(Long partnerId) {
        return contentRepository.findByPartnerIdOrderByCreatedAtDesc(partnerId).stream()
                .map(ContentService::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void recordView(Long userId, Long contentId) {
        Content content = getEntityById(contentId);
        if (content.getStatus() != ContentStatus.APPROVED) {
            throw new BadRequestException("Content not available");
        }
        if (!subscriptionService.hasActiveSubscription(userId)) {
            throw new BadRequestException("Subscription required to stream content");
        }
        User user = userRepository.getReferenceById(userId);
        ContentView view = ContentView.builder().user(user).content(content).build();
        contentViewRepository.save(view);
        content.setViewsCount(content.getViewsCount() + 1);
        contentRepository.save(content);
    }

    @Transactional
    public void recordDownload(Long userId, Long contentId) {
        Content content = getEntityById(contentId);
        if (content.getStatus() != ContentStatus.APPROVED) {
            throw new BadRequestException("Content not available");
        }
        if (!subscriptionService.hasActiveSubscription(userId)) {
            throw new BadRequestException("Subscription required to download content");
        }
        User user = userRepository.getReferenceById(userId);
        ContentDownload download = ContentDownload.builder().user(user).content(content).build();
        contentDownloadRepository.save(download);
        content.setDownloadsCount(content.getDownloadsCount() + 1);
        contentRepository.save(content);
    }

    public List<ContentResponse> getViewingHistory(Long userId, Pageable pageable) {
        return contentViewRepository.findByUserIdOrderByViewedAtDesc(userId, pageable).stream()
                .map(cv -> toResponse(cv.getContent()))
                .collect(Collectors.toList());
    }

    public List<ContentAnalyticsResponse> getPartnerAnalytics(Long partnerId) {
        return contentRepository.findByPartnerIdOrderByCreatedAtDesc(partnerId).stream()
                .map(c -> ContentAnalyticsResponse.builder()
                        .contentId(c.getId())
                        .title(c.getTitle())
                        .viewsCount(c.getViewsCount())
                        .downloadsCount(c.getDownloadsCount())
                        .build())
                .collect(Collectors.toList());
    }

    public static ContentResponse toResponse(Content c) {
        return ContentResponse.builder()
                .id(c.getId())
                .title(c.getTitle())
                .description(c.getDescription())
                .thumbnail(c.getThumbnail())
                .videoUrl(c.getVideoUrl())
                .audioUrl(c.getAudioUrl())
                .contentType(c.getContentType())
                .status(c.getStatus())
                .partnerId(c.getPartner().getId())
                .partnerName(c.getPartner().getCompanyName())
                .viewsCount(c.getViewsCount())
                .downloadsCount(c.getDownloadsCount())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
