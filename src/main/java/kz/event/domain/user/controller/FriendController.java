package kz.event.domain.user.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import kz.event.domain.user.DTO.FriendNewDto;
import kz.event.domain.user.DTO.FriendUpdateDto;
import kz.event.domain.user.entity.Friends;
import kz.event.domain.user.enums.FriendStatus;
import kz.event.domain.user.repository.FriendRepository;
import kz.event.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "friend methods")
@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
@Slf4j
public class FriendController {
    private final FriendRepository friendRepository;
    private final UserRepository userRepository;


    private FriendStatus getStatus(String status) {
        if ("accepted".equalsIgnoreCase(status)) {
            return FriendStatus.accepted;
        } else if ("rejected".equalsIgnoreCase(status)) {
            return FriendStatus.rejected;
        } else if ("blocked".equalsIgnoreCase(status)) {
            return FriendStatus.rejected;
        } else {
            throw new IllegalArgumentException("Invalid friend status format");
        }
    }

    @PostMapping
    public ResponseEntity<?> newFriend(@Valid @RequestBody FriendNewDto dto, @AuthenticationPrincipal UUID userId) {
        if (!userRepository.existsById(userId) || !userRepository.existsById(dto.getFriendId())) {
            throw new EntityNotFoundException("User not found");
        }

        if (userId.equals(dto.getFriendId())) {
            throw new IllegalArgumentException("Cannot add yourself");
        }

        if (friendRepository.existsBetween(userId, dto.getFriendId())) {
            throw new IllegalStateException("Friend relation already exists");
        }

        friendRepository.save(new Friends(userId, dto.getFriendId()));

        return ResponseEntity.ok("Relation created successfully");
    }

    @PatchMapping
    @Transactional
    public ResponseEntity<?> updateFriend(@Valid @RequestBody FriendUpdateDto dto, @AuthenticationPrincipal UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found");
        }

        Friends friends = friendRepository.findById(dto.getId())
                .orElseThrow(() -> new EntityNotFoundException("Friend relation not found"));

        friends.setStatus(getStatus(dto.getStatus()));

        return ResponseEntity.ok("Relation updated successfully");
    }
}
