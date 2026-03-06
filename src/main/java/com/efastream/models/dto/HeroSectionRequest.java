package com.efastream.models.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class HeroSectionRequest {

    @NotBlank(message = "Title is required")
    private String title;
    private String subtitle;
    private String imageUrl;
    private String link;
    private Boolean active;
}
