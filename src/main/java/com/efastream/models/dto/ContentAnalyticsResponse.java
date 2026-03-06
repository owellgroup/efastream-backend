package com.efastream.models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentAnalyticsResponse {

    private Long contentId;
    private String title;
    private long viewsCount;
    private long downloadsCount;
}
