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
import kz.event.auth.DTO.CodeCheckerDto;
import kz.event.auth.DTO.LoginRequestDto;
import kz.event.auth.DTO.RegisterDto;
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
    public String login(@RequestBody LoginRequestDto request) {
        if (request.getEmail() == null || request.getPassword() == null) {
            throw new IllegalArgumentException("Email and password are required");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!passwordHasher.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String token = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        return token;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterDto registerDto) {
        if (userRepository.existsByEmail(registerDto.getEmail())) {
            throw new EntityExistsException("Email already registered!");
        }
        
        User user = new User(registerDto.getEmail());

        try {
            mailSender.send(user.getEmail(), "Код подтверждения для входа в Event");
        } catch (jakarta.mail.MessagingException | java.io.IOException e) {
            log.error("Failed to send verification email to {}", user.getEmail(), e);
            throw new IllegalStateException("Failed to send verification email");
        }

        log.info("New user: " + userRepository.save(user));
        return ResponseEntity.ok("User created successfully");
    }

    @PostMapping("/check-code")
    @Transactional
    public ResponseEntity<?> codeCheck(@RequestBody CodeCheckerDto codeCheckerDto) {
        if (codeCheckerDto.getEmail() == null || codeCheckerDto.getCode() == null) {
            throw new IllegalArgumentException("Email and code are required");
        }

        if (!mailSender.codeCheck(codeCheckerDto.getEmail(), codeCheckerDto.getCode())) {
            return ResponseEntity.badRequest().body("Invalid verification code");
        }

        User user = userRepository.findByEmail(codeCheckerDto.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        userRepository.updateStatus(user.getId(), UserStatus.active);
        mailSender.deleteCode(codeCheckerDto.getEmail());
        return ResponseEntity.ok("Email verified");
    }
}
