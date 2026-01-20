package kz.event.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import kz.event.auth.DTO.CodeCheckerDto;
import kz.event.auth.DTO.LoginRequestDto;
import kz.event.auth.DTO.RegisterDto;
import kz.event.auth.DTO.SetPasswordDto;
import kz.event.auth.service.JwtService;
import kz.event.auth.service.MailSenderService;
import kz.event.auth.service.PasswordHashingService;
import kz.event.domain.user.entity.User;
import kz.event.domain.user.enums.UserStatus;
import kz.event.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "auth methods")
@Slf4j
public class AuthController {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final MailSenderService mailSender;
    private final PasswordHashingService passwordHasher;

    @PostMapping("/login")
    public String login(@Valid @RequestBody LoginRequestDto dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!passwordHasher.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String token = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        return token;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new EntityExistsException("Email already registered!");
        }
        
        User user = new User(dto.getEmail());

        try {
            mailSender.send(user.getEmail(), "Код подтверждения для входа в Event");
        } catch (jakarta.mail.MessagingException | java.io.IOException e) {
            log.error("Failed to send verification email to {}", user.getEmail(), e);
            throw new IllegalStateException("Failed to send verification email");
        }

        log.info("New user: " + userRepository.save(user));
        return ResponseEntity.ok("User created successfully");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody RegisterDto dto) {
        if (!userRepository.existsByEmail(dto.getEmail())) {
            throw new EntityNotFoundException("User not found");
        }

        try {
            mailSender.send(dto.getEmail(), "Код подтверждения для изменения пароля");
        } catch (jakarta.mail.MessagingException | java.io.IOException e) {
            log.error("Failed to send verification email to {}", dto.getEmail(), e);
            throw new IllegalStateException("Failed to send verification email");
        }
        
        return ResponseEntity.ok("Code sent successfully");
    }

    private void checkCode(String email, String code) {
        if (!mailSender.codeCheck(email, code)) {
            throw new IllegalArgumentException("Invalid verification code");
        } else {
            mailSender.deleteCode(email);
        }
    }

    @PostMapping("/check-code")
    @Transactional
    public ResponseEntity<?> codeCheck(@Valid @RequestBody CodeCheckerDto dto) {
        
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        checkCode(dto.getEmail(), dto.getCode());

        userRepository.updateStatus(user.getId(), UserStatus.active);
        return ResponseEntity.ok("Email verified");
    }

    @PostMapping("/set-password")
    @Transactional
    public ResponseEntity<?> setPassword(@Valid @RequestBody SetPasswordDto dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        userRepository.updatePassword(user.getId(), passwordHasher.hash(dto.getPassword()));

        return ResponseEntity.ok("Password successfully set");
    }
}
