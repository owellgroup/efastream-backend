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
public class PartnerResponse {

    private Long id;
    private String email;
    private String companyName;
    private String contactPerson;
    private String phone;
    private boolean enabled;
    private Instant createdAt;
}
