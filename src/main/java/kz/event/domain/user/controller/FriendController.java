package kz.event.domain.user.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import kz.event.domain.user.DTO.FriendNewDto;
import kz.event.domain.user.DTO.FriendDto;
import kz.event.domain.user.entity.Friend;
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

    private void validate(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found");
        }
    }

    @PostMapping
    public ResponseEntity<?> newFriend(@Valid @RequestBody FriendNewDto dto, @AuthenticationPrincipal UUID userId) {
        validate(userId);

        if (userId.equals(dto.getFriendId())) {
            throw new IllegalArgumentException("Cannot add yourself");
        }

        if (friendRepository.existsBetween(userId, dto.getFriendId())) {
            throw new IllegalStateException("Friend relation already exists");
        }

        friendRepository.save(new Friend(userId, dto.getFriendId()));

        return ResponseEntity.ok("Relation created successfully");
    }

    @PutMapping("/block-user")
    @Transactional
    public ResponseEntity<?> blockUser(@Valid @RequestBody FriendNewDto dto, @AuthenticationPrincipal UUID userId) {
        validate(userId);

        if (userId.equals(dto.getFriendId())) {
            throw new IllegalArgumentException("Cannot block yourself");
        }

        Friend friend = friendRepository.getFriendsByIds(userId, dto.getFriendId());

        if (friend == null) {
            friendRepository.save(new Friend(userId, dto.getFriendId(), FriendStatus.blocked));
        } else {
            if (!friend.getUserId().equals(userId)) {
                UUID friendId = friend.getUserId();
                friend.setUserId(userId);
                friend.setFriendId(friendId);
            }
            friend.setStatus(FriendStatus.blocked);
        }

        return ResponseEntity.ok("User blocked successfully");
    }

    @PatchMapping("/accept-friend-response")
    @Transactional
    public ResponseEntity<?> acceptFriendResponse(@Valid @RequestBody FriendDto dto, @AuthenticationPrincipal UUID userId) {
        validate(userId);

        Friend friend = friendRepository.findById(dto.getId())
                .orElseThrow(() -> new EntityNotFoundException("Friend relation not found"));

        friend.setStatus(FriendStatus.accepted);

        return ResponseEntity.ok("Relation updated successfully");
    }

    @Operation(summary = "Use for remove user from your friends list/blocked list")
    @DeleteMapping
    public ResponseEntity<?> deleteRelation(@Valid @RequestBody FriendDto dto, @AuthenticationPrincipal UUID userId) {
        validate(userId);

        if (!friendRepository.existsById(dto.getId())) {
            throw new EntityNotFoundException("Friend relation not found");
        }

        friendRepository.deleteById(dto.getId());

        return ResponseEntity.ok("Relation deleted successfully");
    }

    @GetMapping("/friends-list")
    public List<Friend> getAllFriends(@AuthenticationPrincipal UUID userId) {
        validate(userId);
        return friendRepository.getAllFriends(userId);
    }
}
