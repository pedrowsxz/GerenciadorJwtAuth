package com.api.GerenciadorJwtAuth.service;

import com.api.GerenciadorJwtAuth.dto.AuthRequest;
import com.api.GerenciadorJwtAuth.dto.AuthResponse;
import com.api.GerenciadorJwtAuth.exception.UnauthorizedException;
import com.api.GerenciadorJwtAuth.model.Role;
import com.api.GerenciadorJwtAuth.model.User;
import com.api.GerenciadorJwtAuth.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthService authService;

    private AuthRequest authRequest;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        authRequest = new AuthRequest("123.456.789-01", "password123");

        User user = User.builder()
                .id(1L)
                .name("Test User")
                .cpf("123.456.789-01")
                .password("encoded_password")
                .role(Role.USER)
                .build();

        authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    @Test
    void authenticate_WithValidCredentials_ShouldReturnToken() {
        // Given
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("test.jwt.token");

        // When
        AuthResponse response = authService.authenticate(authRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("test.jwt.token");
        assertThat(response.getType()).isEqualTo("Bearer");
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Test User");
        assertThat(response.getCpf()).isEqualTo("123.456.789-01");
        assertThat(response.getRole()).isEqualTo("USER");
    }

    @Test
    void authenticate_WithInvalidCredentials_ShouldThrowException() {
        // Given
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // When & Then
        assertThrows(UnauthorizedException.class, () -> {
            authService.authenticate(authRequest);
        });
    }
}