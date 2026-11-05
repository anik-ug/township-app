package com.anik.township.auth;

import com.anik.township.domain.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final Duration TOKEN_LIFETIME = Duration.ofHours(8);

    private final String secret;
    private SecretKey signingKey;

    public JwtService(@Value("${app.jwt-secret}") String secret) {
        this.secret = secret;
    }

    @PostConstruct
    void validateSecret() {
        if (secret.length() < 32) {
            throw new IllegalStateException("app.jwt-secret must be at least 32 characters");
        }
        signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String issueToken(AppUser user) {
        Instant now = Instant.now();
        long flatId = user.getFlat() == null ? 0L : user.getFlat().getId();
        return Jwts.builder()
                .subject(user.getPhone())
                .claim("userId", user.getId())
                .claim("role", user.getRole().name())
                .claim("flatId", flatId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(TOKEN_LIFETIME)))
                .signWith(signingKey)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
