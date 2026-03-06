package com.efastream.services;

import com.efastream.config.exception.BadRequestException;
import com.efastream.config.exception.ResourceNotFoundException;
import com.efastream.models.dto.PartnerRequest;
import com.efastream.models.dto.PartnerResponse;
import com.efastream.models.entity.Partner;
import com.efastream.repositories.PartnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PartnerService {

    private final PartnerRepository partnerRepository;
    private final PasswordEncoder passwordEncoder;

    public Partner getEntityById(Long id) {
        return partnerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Partner", id));
    }

    public Partner getEntityByEmail(String email) {
        return partnerRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Partner not found"));
    }

    @Transactional
    public PartnerResponse createPartner(PartnerRequest request) {
        if (partnerRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Partner with this email already exists");
        }
        Partner partner = Partner.builder()
                .email(request.getEmail().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .companyName(request.getCompanyName())
                .contactPerson(request.getContactPerson())
                .phone(request.getPhone())
                .enabled(true)
                .build();
        partner = partnerRepository.save(partner);
        return toResponse(partner);
    }

    @Transactional
    public PartnerResponse updatePartner(Long id, PartnerRequest request) {
        Partner partner = getEntityById(id);
        if (!partner.getEmail().equals(request.getEmail()) && partnerRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already in use");
        }
        partner.setEmail(request.getEmail().toLowerCase());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            partner.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        partner.setCompanyName(request.getCompanyName());
        partner.setContactPerson(request.getContactPerson());
        partner.setPhone(request.getPhone());
        partner = partnerRepository.save(partner);
        return toResponse(partner);
    }

    public PartnerResponse getPartner(Long id) {
        return toResponse(getEntityById(id));
    }

    public java.util.List<PartnerResponse> getAllPartners() {
        return partnerRepository.findAll().stream().map(PartnerService::toResponse).toList();
    }

    @Transactional
    public void deletePartner(Long id) {
        if (!partnerRepository.existsById(id)) throw new ResourceNotFoundException("Partner", id);
        partnerRepository.deleteById(id);
    }

    public static PartnerResponse toResponse(Partner p) {
        return PartnerResponse.builder()
                .id(p.getId())
                .email(p.getEmail())
                .companyName(p.getCompanyName())
                .contactPerson(p.getContactPerson())
                .phone(p.getPhone())
                .enabled(p.isEnabled())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
