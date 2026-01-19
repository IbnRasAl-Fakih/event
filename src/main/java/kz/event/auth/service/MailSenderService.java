package kz.event.auth.service;

import java.util.Map;

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
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailSenderService {
    private final JavaMailSender mailSender;
    private final Map<String, String> codes;
    
    @Value("${spring.mail.username}")
    private String from;
    

    private String generateZeroPaddedCode() {
        SecureRandom random = new SecureRandom();
        int code = random.nextInt(1_000_000);
        return String.format("%06d", code);
    }

    public static String normalize(String email) {
        return email.trim().toLowerCase();
    }

    public void send(String to, String subject) throws MessagingException, IOException {
        String code = generateZeroPaddedCode();
        String email = normalize(to);

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

    public boolean codeCheck(String email, String code) {
        return code.equals(codes.get(normalize(email)));
    }

    public void deleteCode(String email) {
        codes.remove(email);
    }
}
