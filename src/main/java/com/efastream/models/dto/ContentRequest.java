package com.efastream.models.dto;

import com.efastream.models.enums.ContentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ContentRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;
    private String thumbnail;
    private String videoUrl;
    private String audioUrl;

    @NotNull(message = "Content type is required")
    private ContentType contentType;
}
