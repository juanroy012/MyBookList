package com.juanroy.mybooklist.auth.service;

import com.juanroy.mybooklist.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private User testUser;

    // Valid Base64-encoded key that decodes to 36 bytes (> 32 required for HS256)
    private static final String SECRET = "dGVzdC1zZWNyZXQta2V5LWZvci10ZXN0aW5nLXB1cnBvc2Vz";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3600000L);

        testUser = new User("testuser", "test@example.com", "password");
        testUser.setEnabled(true);
    }

    // ── generateToken ───────────────────────────────────────────────

    @Test
    void generateToken_returnsNonNullNonEmptyString() {
        String token = jwtService.generateToken(testUser);
        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    void generateToken_producesThreePartJwt() {
        String token = jwtService.generateToken(testUser);
        assertThat(token.split("\\.")).hasSize(3);
    }

    // ── extractUsername ─────────────────────────────────────────────

    @Test
    void extractUsername_returnsEmailUsedAsSubject() {
        String token = jwtService.generateToken(testUser);
        assertThat(jwtService.extractUsername(token)).isEqualTo("test@example.com");
    }

    // ── isTokenValid ────────────────────────────────────────────────

    @Test
    void isTokenValid_withMatchingUser_returnsTrue() {
        String token = jwtService.generateToken(testUser);
        assertThat(jwtService.isTokenValid(token, testUser)).isTrue();
    }

    @Test
    void isTokenValid_withDifferentUser_returnsFalse() {
        String token = jwtService.generateToken(testUser);
        User otherUser = new User("other", "other@example.com", "password");
        otherUser.setEnabled(true);
        assertThat(jwtService.isTokenValid(token, otherUser)).isFalse();
    }

    @Test
    void isTokenValid_withExpiredToken_throwsException() {
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", -1000L);
        String token = jwtService.generateToken(testUser);
        assertThatThrownBy(() -> jwtService.isTokenValid(token, testUser))
                .isInstanceOf(RuntimeException.class);
    }

    // ── getExpirationTime ───────────────────────────────────────────

    @Test
    void getExpirationTime_returnsConfiguredValue() {
        assertThat(jwtService.getExpirationTime()).isEqualTo(3600000L);
    }

    // ── extractExpirationTime ───────────────────────────────────────

    @Test
    void extractExpirationTime_returnsDateInFuture() {
        String token = jwtService.generateToken(testUser);
        assertThat(jwtService.extractExpirationTime(token))
                .isAfter(new java.util.Date());
    }
}

