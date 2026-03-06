package com.efastream.models.dto;

import com.efastream.models.enums.ContentStatus;
import com.efastream.models.enums.ContentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentResponse {

    private Long id;
    private String title;
    private String description;
    private String thumbnail;
    private String videoUrl;
    private String audioUrl;
    private ContentType contentType;
    private ContentStatus status;
    private Long partnerId;
    private String partnerName;
    private long viewsCount;
    private long downloadsCount;
    private Instant createdAt;
}
