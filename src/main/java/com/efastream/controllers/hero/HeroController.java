package com.efastream.controllers.hero;

import com.efastream.models.dto.ApiResponse;
import com.efastream.models.dto.HeroSectionResponse;
import com.efastream.services.HeroSectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/hero")
@RequiredArgsConstructor
public class HeroController {

    private final HeroSectionService heroSectionService;

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<HeroSectionResponse>>> getActive() {
        return ResponseEntity.ok(ApiResponse.success(heroSectionService.getActiveHeroSections()));
    }
}
