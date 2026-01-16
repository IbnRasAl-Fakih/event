package kz.event.domain.user.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import kz.event.domain.user.DTO.UserProfileDto;
import kz.event.domain.user.entity.UserProfile;
import kz.event.domain.user.enums.UserSex;
import kz.event.domain.user.repository.UserProfileRepository;
import kz.event.domain.user.repository.UserRepository;

@Tag(name = "user profile methods")
@RestController
@RequestMapping("/api/user-profiles")
@RequiredArgsConstructor
public class UserProfileController {
    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> addUserData(@RequestBody UserProfileDto dto, @AuthenticationPrincipal UUID userId) {
        if (dto.getUsername() == null || dto.getUsername().isBlank() ||
             dto.getFullName() == null || dto.getFullName().isBlank() ||
             dto.getCity() == null || dto.getCity().isBlank() ||
             dto.getBirthdate() == null || 
             dto.getJob() == null || dto.getJob().isBlank() ||
             dto.getSex() == null || dto.getSex().isBlank()) {
            throw new IllegalArgumentException("Required arguments are missing!");
        }

        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found");
        }

        if (userProfileRepository.existsByUsername(dto.getUsername())) {
            throw new EntityExistsException("Username already registered!");
        }

        UserSex sex;
        if ("male".equalsIgnoreCase(dto.getSex())) {
            sex = UserSex.male;
        } else if ("female".equalsIgnoreCase(dto.getSex())) {
            sex = UserSex.female;
        } else {
            throw new IllegalArgumentException("Invalid sex format");
        }

        userProfileRepository.save(new UserProfile(userId, dto.getUsername(), dto.getFullName(), dto.getBio(), dto.getJob(), dto.getCity(), dto.getBirthdate(), sex));

        return ResponseEntity.ok("User data saved successfully");
    }

    @GetMapping
    public ResponseEntity<?> getUserProfileById(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found");
        }

        return ResponseEntity.ok(userProfileRepository.findById(userId));
    }

    
}
