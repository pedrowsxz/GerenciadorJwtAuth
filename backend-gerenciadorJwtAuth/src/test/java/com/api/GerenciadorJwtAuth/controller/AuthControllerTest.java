package com.api.GerenciadorJwtAuth.controller;

import com.api.GerenciadorJwtAuth.dto.AuthRequest;
import com.api.GerenciadorJwtAuth.dto.AuthResponse;
import com.api.GerenciadorJwtAuth.exception.UnauthorizedException;
import com.api.GerenciadorJwtAuth.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    void authenticateUser_ValidCredentials_ReturnsAuthResponse() throws Exception {
        // Setup valid AuthRequest
        AuthRequest authRequest = new AuthRequest("123.456.789-01", "password123");

        // Mock AuthResponse
        AuthResponse mockResponse = AuthResponse.builder()
                .token("test.jwt.token")
                .type("Bearer")
                .id(1L)
                .name("Test User")
                .cpf("123.456.789-01")
                .role("USER")
                .build();

        when(authService.authenticate(any(AuthRequest.class))).thenReturn(mockResponse);

        // Perform POST request and verify response
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test.jwt.token"))
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.cpf").value("123.456.789-01"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void authenticateUser_InvalidCredentials_ReturnsUnauthorized() throws Exception {
        AuthRequest authRequest = new AuthRequest("123.456.789-01", "wrong_password");

        when(authService.authenticate(any(AuthRequest.class)))
                .thenThrow(new UnauthorizedException("Invalid credentials"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticateUser_InvalidRequest_ReturnsBadRequest() throws Exception {
        // Test missing password
        AuthRequest invalidRequest = new AuthRequest("123.456.789-01", "");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        // Test invalid CPF length
        AuthRequest invalidCpfRequest = new AuthRequest("12345", "password123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCpfRequest)))
                .andExpect(status().isBadRequest());
    }
}