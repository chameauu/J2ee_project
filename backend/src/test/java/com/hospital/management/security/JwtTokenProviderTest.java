package com.hospital.management.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", 
                "mySecretKeyForJWTTokenGenerationThatIsAtLeast256BitsLong12345678");
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", 86400000L);
    }

    @Test
    void shouldGenerateToken() {
        // Given
        String email = "test@example.com";
        String role = "DOCTOR";

        // When
        String token = jwtTokenProvider.generateToken(email, role);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void shouldExtractEmailFromToken() {
        // Given
        String email = "test@example.com";
        String role = "DOCTOR";
        String token = jwtTokenProvider.generateToken(email, role);

        // When
        String extractedEmail = jwtTokenProvider.getEmailFromToken(token);

        // Then
        assertEquals(email, extractedEmail);
    }

    @Test
    void shouldExtractRoleFromToken() {
        // Given
        String email = "test@example.com";
        String role = "DOCTOR";
        String token = jwtTokenProvider.generateToken(email, role);

        // When
        String extractedRole = jwtTokenProvider.getRoleFromToken(token);

        // Then
        assertEquals(role, extractedRole);
    }

    @Test
    void shouldValidateValidToken() {
        // Given
        String email = "test@example.com";
        String role = "DOCTOR";
        String token = jwtTokenProvider.generateToken(email, role);

        // When
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Then
        assertTrue(isValid);
    }

    @Test
    void shouldNotValidateInvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void shouldNotValidateMalformedToken() {
        // Given
        String malformedToken = "not-a-jwt-token";

        // When
        boolean isValid = jwtTokenProvider.validateToken(malformedToken);

        // Then
        assertFalse(isValid);
    }
}
