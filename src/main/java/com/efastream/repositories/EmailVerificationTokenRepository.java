package com.efastream.repositories;

import com.efastream.models.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findByTokenAndUsedFalseAndExpiresAtAfter(String token, Instant now);

    Optional<EmailVerificationToken> findByUserIdAndUsedFalse(Long userId);
}
