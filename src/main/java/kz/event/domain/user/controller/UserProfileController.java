package kz.event.domain.user.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import kz.event.domain.user.DTO.UserProfileDto;
import kz.event.domain.user.DTO.UserProfileUpdateDto;
import kz.event.domain.user.DTO.UsernameDto;
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

    private UserSex getSex(String rawSex) {
        if ("male".equalsIgnoreCase(rawSex)) {
            return UserSex.male;
        } else if ("female".equalsIgnoreCase(rawSex)) {
            return UserSex.female;
        } else {
            throw new IllegalArgumentException("Invalid sex format");
        }
    }

    @PostMapping
    public ResponseEntity<?> addUserData(@Valid @RequestBody UserProfileDto dto, @AuthenticationPrincipal UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found");
        }

        if (userProfileRepository.existsByUsername(dto.getUsername())) {
            throw new EntityExistsException("Username already registered!");
        }

        userProfileRepository.save(new UserProfile(userId, dto.getUsername(), dto.getFullName(), dto.getBio(), dto.getJob(), dto.getCity(), dto.getBirthdate(), getSex(dto.getSex())));

        return ResponseEntity.ok("User data saved successfully");
    }

    @PatchMapping   
    @Transactional
    public ResponseEntity<?> updateUserData(@Valid @RequestBody UserProfileUpdateDto dto, @AuthenticationPrincipal UUID userId) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("UserProfile not found"));

        profile.setFullName(dto.getFullName());
        profile.setBio(dto.getBio());
        profile.setJob(dto.getJob());
        profile.setCity(dto.getCity());
        profile.setBirthdate(dto.getBirthdate());

        return ResponseEntity.ok("User data saved successfully");
    }

    @PostMapping("/is-username-unique")
    public ResponseEntity<?> isUsernameUnique(@Valid @RequestBody UsernameDto dto) {
        if (userProfileRepository.existsByUsername(dto.getUsername())) {
            return ResponseEntity.ok(false);
        }
        return ResponseEntity.ok(true);
    }

    @GetMapping
    public ResponseEntity<?> getUserProfileById(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found");
        }

        return ResponseEntity.ok(userProfileRepository.findById(userId));
    }
}
