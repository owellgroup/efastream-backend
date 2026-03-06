package com.efastream.models.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "password_reset_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String token;

    @Column(nullable = false, length = 50)
    private String userType;  // USER | PARTNER

    @Column(nullable = false)
    private Long userOrPartnerId;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Boolean used;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        if (used == null) used = false;
    }
}
