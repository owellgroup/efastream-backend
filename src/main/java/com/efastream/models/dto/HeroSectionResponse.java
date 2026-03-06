package com.efastream.models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeroSectionResponse {

    private Long id;
    private String title;
    private String subtitle;
    private String imageUrl;
    private String link;
    private boolean active;
    private Instant createdAt;
}
