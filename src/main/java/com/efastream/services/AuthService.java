package com.efastream.services;

import com.efastream.config.JwtConfig;
import com.efastream.config.UnifiedUserDetails;
import com.efastream.config.exception.BadRequestException;
import com.efastream.config.exception.UnauthorizedException;
import com.efastream.models.dto.*;
import com.efastream.models.entity.EmailVerificationToken;
import com.efastream.models.entity.Partner;
import com.efastream.models.entity.PasswordResetToken;
import com.efastream.models.entity.User;
import com.efastream.models.enums.RoleName;
import com.efastream.repositories.AdminRepository;
import com.efastream.repositories.EmailVerificationTokenRepository;
import com.efastream.repositories.PasswordResetTokenRepository;
import com.efastream.repositories.PartnerRepository;
import com.efastream.repositories.RoleRepository;
import com.efastream.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PartnerRepository partnerRepository;
    private final AdminRepository adminRepository;
    private final RoleRepository roleRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtConfig jwtConfig;
    private final EmailService emailService;

    private static final int EMAIL_VERIFICATION_EXPIRATION_HOURS = 24;
    private static final int PASSWORD_RESET_EXPIRATION_HOURS = 1;

    @Transactional
    public ApiResponse<JwtResponse> login(LoginRequest request) {
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        UnifiedUserDetails details = (UnifiedUserDetails) auth.getPrincipal();
        String accessToken = jwtConfig.generateAccessToken(details.id(), details.getUsername(), details.role());
        String refreshToken = jwtConfig.generateRefreshToken(details.id(), details.role());
        return ApiResponse.success(JwtResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtConfig.getExpirationMs() / 1000)
                .userId(details.id())
                .email(details.getUsername())
                .roles(Set.of(details.role()))
                .build());
    }

    @Transactional
    public ApiResponse<String> register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }
        var userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new BadRequestException("Role not found"));
        User user = User.builder()
                .email(request.getEmail().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .emailVerified(false)
                .enabled(true)
                .roles(Set.of(userRole))
                .build();
        userRepository.save(user);

        String token = UUID.randomUUID().toString();
        EmailVerificationToken evt = EmailVerificationToken.builder()
                .token(token)
                .user(user)
                .expiresAt(Instant.now().plusSeconds(EMAIL_VERIFICATION_EXPIRATION_HOURS * 3600L))
                .used(false)
                .build();
        emailVerificationTokenRepository.save(evt);
        emailService.sendVerificationEmail(user.getEmail(), user.getFirstName(), token);
        return ApiResponse.success("Registration successful. Please verify your email.", "Check your email for verification link.");
    }

    @Transactional
    public ApiResponse<String> verifyEmail(String token) {
        EmailVerificationToken evt = emailVerificationTokenRepository
                .findByTokenAndUsedFalseAndExpiresAtAfter(token, Instant.now())
                .orElseThrow(() -> new BadRequestException("Invalid or expired verification token"));
        User user = evt.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);
        evt.setUsed(true);
        emailVerificationTokenRepository.save(evt);
        emailService.sendWelcomeEmail(user.getEmail(), user.getFirstName());
        return ApiResponse.success("Email verified. You can now log in.");
    }

    public ApiResponse<JwtResponse> refreshToken(String refreshToken) {
        if (refreshToken == null || !refreshToken.startsWith("Bearer "))
            throw new UnauthorizedException("Invalid refresh token");
        String token = refreshToken.substring(7);
        var claims = jwtConfig.parseToken(token);
        String type = claims.get("type", String.class);
        if (!"REFRESH".equals(type)) throw new UnauthorizedException("Invalid token type");
        Long entityId = Long.parseLong(claims.getSubject());
        String role = claims.get("role", String.class);
        String email = null;
        if (RoleName.ROLE_USER.name().equals(role)) {
            email = userRepository.findById(entityId).map(User::getEmail).orElse(null);
        } else if (RoleName.ROLE_PARTNER.name().equals(role)) {
            email = partnerRepository.findById(entityId).map(Partner::getEmail).orElse(null);
        } else if (RoleName.ROLE_ADMIN.name().equals(role)) {
            email = adminRepository.findById(entityId).map(com.efastream.models.entity.Admin::getEmail).orElse(null);
        }
        if (email == null) throw new UnauthorizedException("User not found");
        String accessToken = jwtConfig.generateAccessToken(entityId, email, role);
        String newRefresh = jwtConfig.generateRefreshToken(entityId, role);
        return ApiResponse.success(JwtResponse.builder()
                .accessToken(accessToken)
                .refreshToken(newRefresh)
                .tokenType("Bearer")
                .expiresIn(jwtConfig.getExpirationMs() / 1000)
                .userId(entityId)
                .email(email)
                .roles(Set.of(role))
                .build());
    }

    @Transactional
    public ApiResponse<String> forgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail().toLowerCase();
        String userType = request.getUserType().toUpperCase();
        if (!"USER".equals(userType) && !"PARTNER".equals(userType)) {
            throw new BadRequestException("User type must be USER or PARTNER");
        }
        Long entityId = null;
        String name = null;
        if ("USER".equals(userType)) {
            var u = userRepository.findByEmail(email);
            if (u.isPresent()) {
                entityId = u.get().getId();
                name = u.get().getFirstName();
            }
        } else {
            var p = partnerRepository.findByEmail(email);
            if (p.isPresent()) {
                entityId = p.get().getId();
                name = p.get().getCompanyName();
            }
        }
        if (entityId != null) {
            String token = UUID.randomUUID().toString();
            PasswordResetToken prt = PasswordResetToken.builder()
                    .token(token)
                    .userType(userType)
                    .userOrPartnerId(entityId)
                    .expiresAt(Instant.now().plusSeconds(PASSWORD_RESET_EXPIRATION_HOURS * 3600L))
                    .used(false)
                    .build();
            passwordResetTokenRepository.save(prt);
            emailService.sendPasswordResetEmail(email, name != null ? name : "User", token);
        }
        return ApiResponse.success("If the email exists, a reset link has been sent.");
    }

    @Transactional
    public ApiResponse<String> resetPassword(PasswordResetRequest request) {
        var prt = passwordResetTokenRepository
                .findByTokenAndUsedFalseAndExpiresAtAfter(request.getToken(), Instant.now())
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));
        if ("USER".equals(prt.getUserType())) {
            User user = userRepository.findById(prt.getUserOrPartnerId())
                    .orElseThrow(() -> new BadRequestException("User not found"));
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);
        } else if ("PARTNER".equals(prt.getUserType())) {
            Partner partner = partnerRepository.findById(prt.getUserOrPartnerId())
                    .orElseThrow(() -> new BadRequestException("Partner not found"));
            partner.setPassword(passwordEncoder.encode(request.getNewPassword()));
            partnerRepository.save(partner);
        }
        prt.setUsed(true);
        passwordResetTokenRepository.save(prt);
        return ApiResponse.success("Password reset successful.");
    }
}
