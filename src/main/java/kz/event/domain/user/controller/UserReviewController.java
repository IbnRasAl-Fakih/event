package kz.event.domain.user.controller;

import kz.event.domain.user.DTO.CreateReviewDto;
import kz.event.domain.user.DTO.DeleteReviewDto;
import kz.event.domain.user.entity.UserReview;
import kz.event.domain.user.repository.UserRepository;
import kz.event.domain.user.repository.UserReviewRepository;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "user review methods")
@RestController
@RequestMapping("/api/user-review")
@RequiredArgsConstructor
@Slf4j
public class UserReviewController {
    private final UserReviewRepository userReviewRepository;
    private final UserRepository userRepository;

    private void validate(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found");
        }
    }

    @GetMapping("/me")
    public List<UserReview> getReviewsMe(@AuthenticationPrincipal UUID userId) {
        return userReviewRepository.findByRecipientId(userId);
    }

    @GetMapping("/by-user-id")
    public List<UserReview> getReviewsByUserId(@RequestParam UUID recipientId) {
        return userReviewRepository.findByRecipientId(recipientId);
    }

    @PostMapping
    public ResponseEntity<?> createReview(@AuthenticationPrincipal UUID userId, CreateReviewDto dto) {
        validate(userId);

        if (userId.equals(dto.getRecipientId())) {
            throw new IllegalArgumentException("Cannot evaluate yourself");
        }

        if (userReviewRepository.existsByAuthorIdAndRecipientIdAndEventId(userId, dto.getRecipientId(), dto.getEventId())) {
            throw new IllegalStateException("Review already exists");
        }

        userReviewRepository.save(new UserReview(userId, dto.getRecipientId(), dto.getEventId(), dto.getRating(), dto.getText()));

        return ResponseEntity.ok("Review created successfully");
    }

    @DeleteMapping
    public ResponseEntity<?> deleteReviewById(@AuthenticationPrincipal UUID userId, DeleteReviewDto dto) {
        validate(userId);

        if (!userReviewRepository.existsById(dto.getId())) {
            throw new EntityNotFoundException("Review not found!");
        }

        userReviewRepository.deleteById(dto.getId());

        return ResponseEntity.ok("Review deleted successfully");
    }

    @GetMapping("/by-review-id")
    public UserReview getReviewById(@RequestParam UUID id) {
        return userReviewRepository.findById(id).orElseThrow();
    }
}
