package com.api.GerenciadorJwtAuth.service;

import com.api.GerenciadorJwtAuth.dto.AuthRequest;
import com.api.GerenciadorJwtAuth.dto.AuthResponse;
import com.api.GerenciadorJwtAuth.exception.UnauthorizedException;
import com.api.GerenciadorJwtAuth.model.User;
import com.api.GerenciadorJwtAuth.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthResponse authenticate(AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getCpf(), request.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.generateToken(authentication);

            User userDetails = (User) authentication.getPrincipal();

            return AuthResponse.builder()
                    .token(jwt)
                    .type("Bearer")
                    .id(userDetails.getId())
                    .name(userDetails.getName())
                    .cpf(userDetails.getCpf())
                    .role(userDetails.getRole().name())
                    .build();

        } catch (Exception e) {
            throw new UnauthorizedException("Invalid CPF or password");
        }
    }
}