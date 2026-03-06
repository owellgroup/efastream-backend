package com.efastream.services;

import com.efastream.config.UnifiedUserDetails;
import com.efastream.config.exception.BadRequestException;
import com.efastream.models.entity.Admin;
import com.efastream.models.entity.Partner;
import com.efastream.models.entity.User;
import com.efastream.models.enums.RoleName;
import com.efastream.repositories.AdminRepository;
import com.efastream.repositories.PartnerRepository;
import com.efastream.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UnifiedUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PartnerRepository partnerRepository;
    private final AdminRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            User u = user.get();
            return new UnifiedUserDetails(u.getId(), u.getEmail(), u.getPassword(),
                    RoleName.ROLE_USER.name(), u.isEnabled());
        }
        var partner = partnerRepository.findByEmail(email);
        if (partner.isPresent()) {
            Partner p = partner.get();
            return new UnifiedUserDetails(p.getId(), p.getEmail(), p.getPassword(),
                    RoleName.ROLE_PARTNER.name(), p.isEnabled());
        }
        var admin = adminRepository.findByEmail(email);
        if (admin.isPresent()) {
            Admin a = admin.get();
            return new UnifiedUserDetails(a.getId(), a.getEmail(), a.getPassword(),
                    RoleName.ROLE_ADMIN.name(), a.isEnabled());
        }
        throw new UsernameNotFoundException("User not found: " + email);
    }

    public UnifiedUserDetails loadUserByIdAndType(Long id, String type) {
        return switch (type.toUpperCase()) {
            case "USER" -> userRepository.findById(id)
                    .map(u -> new UnifiedUserDetails(u.getId(), u.getEmail(), u.getPassword(), RoleName.ROLE_USER.name(), u.isEnabled()))
                    .orElseThrow(() -> new BadRequestException("User not found"));
            case "PARTNER" -> partnerRepository.findById(id)
                    .map(p -> new UnifiedUserDetails(p.getId(), p.getEmail(), p.getPassword(), RoleName.ROLE_PARTNER.name(), p.isEnabled()))
                    .orElseThrow(() -> new BadRequestException("Partner not found"));
            case "ADMIN" -> adminRepository.findById(id)
                    .map(a -> new UnifiedUserDetails(a.getId(), a.getEmail(), a.getPassword(), RoleName.ROLE_ADMIN.name(), a.isEnabled()))
                    .orElseThrow(() -> new BadRequestException("Admin not found"));
            default -> throw new BadRequestException("Invalid user type");
        };
    }
}
