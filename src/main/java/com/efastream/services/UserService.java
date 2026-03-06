package com.efastream.services;

import com.efastream.config.exception.ResourceNotFoundException;
import com.efastream.models.dto.UserResponse;
import com.efastream.models.entity.User;
import com.efastream.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getEntityById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    public UserResponse getProfile(Long userId) {
        User user = getEntityById(userId);
        return toResponse(user);
    }

    public List<UserResponse> getAll() {
        return userRepository.findAll().stream().map(UserService::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public UserResponse update(Long id, String firstName, String lastName, Boolean enabled) {
        User user = getEntityById(id);
        if (firstName != null) user.setFirstName(firstName);
        if (lastName != null) user.setLastName(lastName);
        if (enabled != null) user.setEnabled(enabled);
        user = userRepository.save(user);
        return toResponse(user);
    }

    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) throw new ResourceNotFoundException("User", id);
        userRepository.deleteById(id);
    }

    public static UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .emailVerified(user.isEmailVerified())
                .enabled(user.isEnabled())
                .roles(user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toSet()))
                .createdAt(user.getCreatedAt())
                .build();
    }
}
