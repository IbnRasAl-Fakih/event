package kz.event.domain.user.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import kz.event.domain.user.DTO.UserDto;
import kz.event.domain.user.entity.User;
import kz.event.domain.user.repository.UserRepository;
import kz.event.domain.user.service.MailSenderService;
import kz.event.domain.user.service.PasswordHashingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "user methods")
@RestController
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserRepository userRepository;
    private final MailSenderService mailSender;
    private final PasswordHashingService passwordHasher;

    @PostMapping("/api/users/add")
    public String addUser(@RequestBody UserDto userDto) {
        if (userDto.getEmail() == null || userDto.getPassword() == null) {
            throw new IllegalArgumentException("Required arguments are null!");
        }

        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new EntityExistsException("Email already registered!");
        }
        
        User user = new User(userDto.getEmail(), userDto.getPhone(), passwordHasher.hash(userDto.getPassword()));

        try {
            mailSender.send(user.getEmail(), "Код подтверждения для входа в Event");
        } catch (jakarta.mail.MessagingException | java.io.IOException e) {
            log.error("Failed to send verification email to {}", user.getEmail(), e);
            throw new IllegalStateException("Failed to send verification email");
        }

        log.info("New user: " + userRepository.save(user));
        return "User created successfully";
    }

    @GetMapping("api/users/all")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("api/users")
    public User getUserById(@RequestParam UUID id) {
        if (userRepository.existsById(id)) {
            return userRepository.findById(id).orElseThrow();
        }
        throw new EntityNotFoundException("User not found!");
    }

    @DeleteMapping("api/users")
    public void deleteUserById(@RequestParam UUID id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
        } else {
            throw new EntityNotFoundException("User not found!");
        }
    }
}
