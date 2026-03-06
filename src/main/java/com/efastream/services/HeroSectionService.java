package com.efastream.services;

import com.efastream.config.exception.ResourceNotFoundException;
import com.efastream.models.dto.HeroSectionRequest;
import com.efastream.models.dto.HeroSectionResponse;
import com.efastream.models.entity.HeroSection;
import com.efastream.repositories.HeroSectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HeroSectionService {

    private final HeroSectionRepository heroSectionRepository;

    public List<HeroSectionResponse> getActiveHeroSections() {
        return heroSectionRepository.findByActiveTrueOrderByIdAsc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public HeroSectionResponse getById(Long id) {
        return toResponse(getEntityById(id));
    }

    public HeroSection getEntityById(Long id) {
        return heroSectionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("HeroSection", id));
    }

    @Transactional
    public HeroSectionResponse create(HeroSectionRequest request) {
        HeroSection hero = HeroSection.builder()
                .title(request.getTitle())
                .subtitle(request.getSubtitle())
                .imageUrl(request.getImageUrl())
                .link(request.getLink())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();
        hero = heroSectionRepository.save(hero);
        return toResponse(hero);
    }

    @Transactional
    public HeroSectionResponse update(Long id, HeroSectionRequest request) {
        HeroSection hero = getEntityById(id);
        hero.setTitle(request.getTitle());
        hero.setSubtitle(request.getSubtitle());
        hero.setImageUrl(request.getImageUrl());
        hero.setLink(request.getLink());
        if (request.getActive() != null) hero.setActive(request.getActive());
        hero = heroSectionRepository.save(hero);
        return toResponse(hero);
    }

    @Transactional
    public void delete(Long id) {
        if (!heroSectionRepository.existsById(id)) throw new ResourceNotFoundException("HeroSection", id);
        heroSectionRepository.deleteById(id);
    }

    public List<HeroSectionResponse> getAll() {
        return heroSectionRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    private HeroSectionResponse toResponse(HeroSection h) {
        return HeroSectionResponse.builder()
                .id(h.getId())
                .title(h.getTitle())
                .subtitle(h.getSubtitle())
                .imageUrl(h.getImageUrl())
                .link(h.getLink())
                .active(h.isActive())
                .createdAt(h.getCreatedAt())
                .build();
    }
}
