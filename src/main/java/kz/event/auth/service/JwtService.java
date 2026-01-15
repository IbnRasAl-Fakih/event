package kz.event.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${security.jwt.access-ttl-minutes}")
    private long accessTtlMinutes;

    @Value("${security.jwt.refresh-ttl-days}")
    private long refreshTtlDays;

    @Value("${security.jwt.issuer}")
    private String issuer;

    private SecretKey key;

    @PostConstruct
    void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(UUID userId, String email, String role) {
        return generateToken(
                userId,
                Map.of(
                        "email", email,
                        "role", role,
                        "token_type", "access"
                ),
                Instant.now().plus(accessTtlMinutes, ChronoUnit.MINUTES)
        );
    }

    public String generateRefreshToken(UUID userId) {
        return generateToken(
                userId,
                Map.of("token_type", "refresh"),
                Instant.now().plus(refreshTtlDays, ChronoUnit.DAYS)
        );
    }

    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean isAccessToken(String token) {
        return "access".equals(getClaims(token).get("token_type", String.class));
    }

    public boolean isRefreshToken(String token) {
        return "refresh".equals(getClaims(token).get("token_type", String.class));
    }

    public UUID getUserId(String token) {
        String sub = getClaims(token).getSubject();
        return UUID.fromString(sub);
    }

    public String getEmail(String token) {
        return getClaims(token).get("email", String.class);
    }

    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    public Instant getExpiration(String token) {
        Date exp = getClaims(token).getExpiration();
        return exp.toInstant();
    }

    private String generateToken(UUID userId, Map<String, Object> extraClaims, Instant expiresAt) {
        Instant now = Instant.now();

        return Jwts.builder()
                .issuer(issuer)
                .subject(userId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .claims(extraClaims)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    private Jws<Claims> parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token);
    }

    private Claims getClaims(String token) {
        return parse(token).getPayload();
    }
}
