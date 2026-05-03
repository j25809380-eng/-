package com.fitnote.backend.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Base64;
import java.util.List;
import javax.crypto.SecretKey;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

    private JwtTokenProvider provider;
    private String secret;

    @BeforeEach
    void setUp() {
        // 生成一个合法的 BASE64 密钥
        SecretKey key = Jwts.SIG.HS384.key().build();
        secret = Base64.getEncoder().encodeToString(key.getEncoded());
        provider = new JwtTokenProvider(secret, 72);
    }

    @Test
    void generateUserToken_shouldBeValid() {
        String token = provider.generateUserToken(1L);
        assertNotNull(token);
        assertTrue(provider.validate(token));
        assertEquals(1L, provider.getUserId(token));
    }

    @Test
    void generateAdminToken_shouldHaveAdminRole() {
        String token = provider.generateAdminToken(1L, "Admin");
        assertTrue(provider.validate(token));
        List<String> roles = provider.getRoles(token);
        assertTrue(roles.contains("ROLE_ADMIN"));
        assertEquals("ADMIN", provider.getTokenType(token));
    }

    @Test
    void invalidToken_shouldFailValidation() {
        assertTrue(provider.validate("invalid.token.here") == false);
    }
}
