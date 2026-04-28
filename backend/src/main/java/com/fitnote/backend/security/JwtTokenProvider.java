package com.fitnote.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private static final String ROLES_KEY = "roles";
    private static final String TOKEN_TYPE_KEY = "tokenType";
    private static final String DISPLAY_NAME_KEY = "displayName";

    private final Key key;
    private final long expireHours;

    public JwtTokenProvider(@Value("${app.jwt.secret}") String secret,
                            @Value("${app.jwt.expire-hours}") long expireHours) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expireHours = expireHours;
    }

    public String generateUserToken(Long userId) {
        return buildToken(userId, List.of("ROLE_USER"), "USER", null);
    }

    public String generateAdminToken(Long adminId, String displayName) {
        return buildToken(adminId, List.of("ROLE_ADMIN"), "ADMIN", displayName);
    }

    public Long getUserId(String token) {
        return Long.parseLong(parseClaims(token).getSubject());
    }

    @SuppressWarnings("unchecked")
    public List<String> getRoles(String token) {
        Object value = parseClaims(token).get(ROLES_KEY);
        if (value instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of();
    }

    public String getTokenType(String token) {
        Object value = parseClaims(token).get(TOKEN_TYPE_KEY);
        return value == null ? "" : String.valueOf(value);
    }

    public boolean validate(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private String buildToken(Long principalId, List<String> roles, String tokenType, String displayName) {
        Instant now = Instant.now();
        var builder = Jwts.builder()
            .subject(String.valueOf(principalId))
            .claim(ROLES_KEY, roles)
            .claim(TOKEN_TYPE_KEY, tokenType)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(expireHours, ChronoUnit.HOURS)))
            .signWith(key);

        if (displayName != null && !displayName.isBlank()) {
            builder.claim(DISPLAY_NAME_KEY, displayName);
        }
        return builder.compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
            .verifyWith((javax.crypto.SecretKey) key)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
