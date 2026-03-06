package com.efastream.config;

import com.efastream.models.entity.Admin;
import com.efastream.models.entity.Role;
import com.efastream.models.enums.RoleName;
import com.efastream.repositories.AdminRepository;
import com.efastream.repositories.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;

@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    private final RoleRepository roleRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner seedData() {
        return args -> {
            for (RoleName name : RoleName.values()) {
                if (roleRepository.findByName(name).isEmpty()) {
                    roleRepository.save(Role.builder().name(name).build());
                }
            }
            if (adminRepository.findByEmail("admin@efastream.com").isEmpty()) {
                Admin admin = Admin.builder()
                        .email("admin@efastream.com")
                        .password(passwordEncoder.encode("Admin@123"))
                        .name("System Admin")
                        .enabled(true)
                        .build();
                adminRepository.save(admin);
            }
        };
    }
}
