package kz.event.domain.user.service;

import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Year;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MailSenderService {
    private final JavaMailSender mailSender;
    private final Map<String, String> codes;
    
    @Value("${spring.mail.username}")
    private String from;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private String generateZeroPaddedCode() {
        SecureRandom random = new SecureRandom();
        int code = random.nextInt(1_000_000);
        return String.format("%06d", code);
    }

    public static void validate(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email must not be empty");
        }

        if (email.length() > 255) {
            throw new IllegalArgumentException("Email is too long");
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    public static String normalize(String email) {
        return email.trim().toLowerCase();
    }

    public void send(String to, String subject) throws MessagingException, IOException {
        String code = generateZeroPaddedCode();
        String email = normalize(to);
        validate(email);

        codes.put(email, code);

        String html = loadTemplate("mail/templates/verification-code.html")
                .replace("{{code}}", code)
                .replace("{{ttlMinutes}}", "10")
                .replace("{{year}}", String.valueOf(Year.now().getValue()));

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(email);
        helper.setSubject(subject);
        helper.setFrom(from);
        helper.setText(html, true);

        helper.addInline("eventLogo", new ClassPathResource("mail/images/logo.png"));

        mailSender.send(message);
    }

    private String loadTemplate(String classpathLocation) throws IOException {
        ClassPathResource resource = new ClassPathResource(classpathLocation);
        try (var in = resource.getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
