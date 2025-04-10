package com.api.GerenciadorJwtAuth.security;

import com.api.GerenciadorJwtAuth.model.Role;
import com.api.GerenciadorJwtAuth.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;
    private final String secret = "YourVeryStrongSecretKeyHereShouldBeAtLeast256BitsLongForBetterSecurity";
    private Authentication authentication;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(tokenProvider, "jwtSecret", secret);
        ReflectionTestUtils.setField(tokenProvider, "jwtExpirationMs", 60000); // 1 minute

        User user = User.builder()
                .id(1L)
                .name("Test User")
                .cpf("12345678901")
                .password("encoded_password")
                .role(Role.USER)
                .build();

        authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        userDetails = new org.springframework.security.core.userdetails.User(
                user.getCpf(),
                user.getPassword(),
                user.getAuthorities()
        );
    }

    @Test
    void generateToken_ShouldReturnValidToken() {
        // When
        String token = tokenProvider.generateToken(authentication);

        // Then
        assertThat(token).isNotNull();
        assertTrue(tokenProvider.validateToken(token, userDetails));
        assertThat(tokenProvider.getCpfFromToken(token)).isEqualTo("12345678901");
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnTrue() {
        // Given
        String token = tokenProvider.generateToken(authentication);

        // When
        boolean isValid = tokenProvider.validateToken(token, userDetails);

        // Then
        assertTrue(isValid);
    }

    @Test
    void validateToken_WithInvalidToken_ShouldReturnFalse() {
        // Given
        String invalidToken = "invalid.token.string";

        // When
        boolean isValid = tokenProvider.validateToken(invalidToken, userDetails);

        // Then
        assertFalse(isValid);
    }

    @Test
    void validateToken_WithExpiredToken_ShouldReturnFalse() {
        // Given
        Key key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
        String expiredToken = Jwts.builder()
                .setSubject("12345678901")
                .setIssuedAt(new Date(System.currentTimeMillis() - 120000)) // 2 minutes ago
                .setExpiration(new Date(System.currentTimeMillis() - 60000)) // 1 minute ago
                .signWith(key)
                .compact();

        // When
        boolean isValid = tokenProvider.validateToken(expiredToken, userDetails);

        // Then
        assertFalse(isValid);
    }

    @Test
    void validateToken_WithWrongUser_ShouldReturnFalse() {
        // Given
        String token = tokenProvider.generateToken(authentication);
        UserDetails wrongUserDetails = new org.springframework.security.core.userdetails.User(
                "different_cpf",
                "password",
                Collections.emptyList()
        );

        // When
        boolean isValid = tokenProvider.validateToken(token, wrongUserDetails);

        // Then
        assertFalse(isValid);
    }

    @Test
    void getCpfFromToken_WithValidToken_ShouldReturnCpf() {
        // Given
        String token = tokenProvider.generateToken(authentication);

        // When
        String cpf = tokenProvider.getCpfFromToken(token);

        // Then
        assertThat(cpf).isEqualTo("12345678901");
    }
}