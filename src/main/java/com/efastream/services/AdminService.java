package com.efastream.services;

import com.efastream.config.exception.BadRequestException;
import com.efastream.config.exception.ResourceNotFoundException;
import com.efastream.models.dto.*;
import com.efastream.models.entity.Admin;
import com.efastream.models.entity.Content;
import com.efastream.models.entity.Partner;
import com.efastream.models.enums.ContentStatus;
import com.efastream.repositories.AdminRepository;
import com.efastream.repositories.ContentRepository;
import com.efastream.repositories.PartnerRepository;
import com.efastream.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final PartnerRepository partnerRepository;
    private final AdminRepository adminRepository;
    private final ContentRepository contentRepository;
    private final EmailService emailService;

    public Admin getEntityById(Long id) {
        return adminRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Admin", id));
    }

    public AdminDashboardResponse getDashboard() {
        long totalUsers = userRepository.count();
        long totalPartners = partnerRepository.count();
        long totalContent = contentRepository.count();
        long totalViews = contentRepository.findAll().stream().mapToLong(Content::getViewsCount).sum();
        long totalDownloads = contentRepository.findAll().stream().mapToLong(Content::getDownloadsCount).sum();
        return AdminDashboardResponse.builder()
                .totalUsers(totalUsers)
                .totalPartners(totalPartners)
                .totalContent(totalContent)
                .totalViews(totalViews)
                .totalDownloads(totalDownloads)
                .build();
    }

    @Transactional
    public ContentResponse approveContent(Long contentId) {
        Content content = contentRepository.findById(contentId).orElseThrow(() -> new ResourceNotFoundException("Content", contentId));
        if (content.getStatus() == ContentStatus.APPROVED) {
            throw new BadRequestException("Content already approved");
        }
        content.setStatus(ContentStatus.APPROVED);
        content = contentRepository.save(content);
        Partner partner = content.getPartner();
        emailService.sendContentApproved(partner.getEmail(), partner.getCompanyName(), content.getTitle());
        return ContentService.toResponse(content);
    }

    @Transactional
    public ContentResponse rejectContent(Long contentId, String reason) {
        Content content = contentRepository.findById(contentId).orElseThrow(() -> new ResourceNotFoundException("Content", contentId));
        if (content.getStatus() == ContentStatus.REJECTED) {
            throw new BadRequestException("Content already rejected");
        }
        content.setStatus(ContentStatus.REJECTED);
        content = contentRepository.save(content);
        Partner partner = content.getPartner();
        emailService.sendContentRejected(partner.getEmail(), partner.getCompanyName(), content.getTitle(), reason);
        return ContentService.toResponse(content);
    }

    public List<ContentResponse> getPendingContent() {
        return contentRepository.findByStatus(ContentStatus.PENDING, org.springframework.data.domain.Pageable.unpaged())
                .stream().map(ContentService::toResponse).collect(Collectors.toList());
    }
}
